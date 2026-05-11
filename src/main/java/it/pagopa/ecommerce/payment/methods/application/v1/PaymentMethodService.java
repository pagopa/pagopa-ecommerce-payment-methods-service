package it.pagopa.ecommerce.payment.methods.application.v1;

import it.pagopa.ecommerce.commons.client.JwtIssuerClient;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.CreateTokenRequestDto;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import it.pagopa.ecommerce.commons.utils.ReactiveUniqueIdUtils;
import it.pagopa.ecommerce.payment.methods.application.BundleOptions;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodServiceCommon;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.client.JwtTokenIssuerClient;
import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.exception.OrderIdNotFoundException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.exception.SessionAlreadyAssociatedToTransaction;
import it.pagopa.ecommerce.payment.methods.infrastructure.*;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.generated.ecommerce.gec.v1.dto.PspSearchCriteriaDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferListItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service(PaymentMethodService.QUALIFIER_NAME)
@ApplicationService
@Slf4j
public class PaymentMethodService extends PaymentMethodServiceCommon {

    protected static final String QUALIFIER_NAME = "paymentMethodService";

    public enum SessionPaymentMethod {
        CARDS("CARDS");

        public final String value;

        SessionPaymentMethod(String value) {
            this.value = value;
        }

        public static SessionPaymentMethod fromValue(String value) {
            for (SessionPaymentMethod method : SessionPaymentMethod.values()) {
                if (method.value.equals(value)) {
                    return method;
                }
            }

            throw new IllegalArgumentException("Invalid session payment method: '%s'".formatted(value));
        }
    }

    private final AfmClient afmClient;

    private final NpgClient npgClient;

    private final PaymentMethodRepository paymentMethodRepository;

    private final PaymentMethodFactory paymentMethodFactory;

    private final SessionUrlConfig sessionUrlConfig;

    private final NpgSessionsTemplateWrapper npgSessionsTemplateWrapper;

    private final String npgDefaultApiKey;

    private final ReactiveUniqueIdUtils uniqueIdUtils;

    private final int npgNotificationTokenValidityTime;

    private final JwtTokenIssuerClient jwtTokenIssuerClient;

    @Autowired
    public PaymentMethodService(
            AfmClient afmClient,
            PaymentMethodRepository paymentMethodRepository,
            PaymentMethodFactory paymentMethodFactory,
            NpgClient npgClient,
            SessionUrlConfig sessionUrlConfig,
            NpgSessionsTemplateWrapper npgSessionsTemplateWrapper,
            @Value("${npg.client.apiKey}") String npgDefaultApiKey,
            ReactiveUniqueIdUtils uniqueIdUtils,
            @Value("${npg.notification.jwt.validity.time}") int npgNotificationTokenValidityTime,
            JwtTokenIssuerClient jwtTokenIssuerClient
    ) {
        super(paymentMethodRepository, npgSessionsTemplateWrapper);
        this.afmClient = afmClient;
        this.npgClient = npgClient;
        this.paymentMethodFactory = paymentMethodFactory;
        this.paymentMethodRepository = paymentMethodRepository;
        this.sessionUrlConfig = sessionUrlConfig;
        this.npgSessionsTemplateWrapper = npgSessionsTemplateWrapper;
        this.npgDefaultApiKey = npgDefaultApiKey;
        this.uniqueIdUtils = uniqueIdUtils;
        this.npgNotificationTokenValidityTime = npgNotificationTokenValidityTime;
        this.jwtTokenIssuerClient = jwtTokenIssuerClient;
    }

    public Mono<PaymentMethod> createPaymentMethod(
                                                   PaymentMethodRequestDto paymentMethodRequestDto

    ) {
        String paymentMethodName = paymentMethodRequestDto.getName();
        String paymentMethodDescription = paymentMethodRequestDto.getDescription();
        PaymentMethodStatusDto statusDto = paymentMethodRequestDto.getStatus();
        List<Pair<Long, Long>> ranges = paymentMethodRequestDto.getRanges().stream()
                .map(r -> Pair.of(r.getMin(), r.getMax()))
                .toList();
        String paymentMethodTypeCode = paymentMethodRequestDto.getPaymentTypeCode();
        String paymentMethodAsset = paymentMethodRequestDto.getAsset();
        PaymentMethodRequestDto.ClientIdEnum clientId = paymentMethodRequestDto.getClientId();
        PaymentMethodManagementTypeDto methodAuthManagement = paymentMethodRequestDto.getMethodManagement();
        Map<String, String> brandAssets = paymentMethodRequestDto.getBrandAssets();
        log.info("[Payment Method Aggregate] Create new aggregate");
        Mono<PaymentMethod> paymentMethod = paymentMethodFactory.newPaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName(paymentMethodName),
                new PaymentMethodDescription(paymentMethodDescription),
                new PaymentMethodStatus(PaymentMethodStatusEnum.valueOf(statusDto.getValue())),
                ranges.stream().map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond())).toList(),
                new PaymentMethodType(paymentMethodTypeCode),
                new PaymentMethodAsset(paymentMethodAsset),
                clientId,
                new PaymentMethodManagement(methodAuthManagement),
                new PaymentMethodBrandAssets(Optional.ofNullable(brandAssets))
        );

        log.info("[Payment Method Aggregate] Store new aggregate");

        return paymentMethod.flatMap(
                p -> paymentMethodRepository.save(
                        new PaymentMethodDocument(
                                p.getPaymentMethodID().value().toString(),
                                p.getPaymentMethodName().value(),
                                p.getPaymentMethodDescription().value(),
                                p.getPaymentMethodStatus().value().toString(),
                                p.getPaymentMethodAsset().value(),
                                p.getPaymentMethodRanges().stream().map(r -> Pair.of(r.min(), r.max()))
                                        .toList(),
                                p.getPaymentMethodTypeCode().value(),
                                p.getClientIdEnum().getValue(),
                                p.getPaymentMethodManagement().value().getValue(),
                                p.getPaymentMethodBrandAsset().brandAssets().orElse(null)
                        )
                ).map(
                        doc -> new PaymentMethod(
                                new PaymentMethodID(UUID.fromString(doc.getPaymentMethodID())),
                                new PaymentMethodName(doc.getPaymentMethodName()),
                                new PaymentMethodDescription(doc.getPaymentMethodDescription()),
                                new PaymentMethodStatus(
                                        PaymentMethodStatusEnum.valueOf(doc.getPaymentMethodStatus())
                                ),
                                new PaymentMethodType(doc.getPaymentMethodTypeCode()),
                                doc.getPaymentMethodRanges().stream()
                                        .map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond()))
                                        .toList(),
                                new PaymentMethodAsset(doc.getPaymentMethodAsset()),
                                clientId,
                                new PaymentMethodManagement(
                                        PaymentMethodManagementTypeDto.valueOf(doc.getMethodManagement())
                                ),
                                new PaymentMethodBrandAssets(Optional.ofNullable(doc.getPaymentMethodsBrandAssets()))
                        )
                )
        );
    }

    public Flux<PaymentMethod> retrievePaymentMethods(
                                                      Integer amount,
                                                      String clientId,
                                                      String deviceVersion
    ) {
        log.info("[Payment Method Aggregate] Retrieve Aggregate");

        return paymentMethodRepository.findByClientId(clientId).filter(
                doc -> amount == null || doc.getPaymentMethodRanges().stream()
                        .anyMatch(
                                range -> range.getFirst().longValue() <= amount
                                        && range.getSecond().longValue() >= amount
                        )
        )
                .sort(
                        (
                         paymentMethodDocument1,
                         paymentMethodDocument2
                        ) -> {
                            if (paymentMethodDocument1.getPaymentMethodTypeCode().equals("CP"))
                                return -1;
                            if (paymentMethodDocument2.getPaymentMethodTypeCode().equals("CP"))
                                return 1;
                            else
                                return paymentMethodDocument1.getPaymentMethodDescription()
                                        .compareTo(paymentMethodDocument2.getPaymentMethodDescription());
                        }
                ).map(this::docToAggregate)
                .map(doc -> filterMethods(doc, clientId, deviceVersion));
    }

    private PaymentMethod filterMethods(
                                        PaymentMethod paymentMethod,
                                        String clientId,
                                        String deviceVersion
    ) {
        /*
         * only for app IO (client id IO) and old app version (deviceVersion parameter
         * null) we should return card payment methods only for onboarding (aka with
         * method management ONBOARDABLE_ONLY) since payment logic to handle card method
         * is implemented in new app only
         */
        if (clientId.equals(ClientIdDto.IO.toString()) && deviceVersion == null) {
            if (paymentMethod.getPaymentMethodTypeCode().value().equals("CP")) {
                return new PaymentMethod(
                        paymentMethod.getPaymentMethodID(),
                        paymentMethod.getPaymentMethodName(),
                        paymentMethod.getPaymentMethodDescription(),
                        paymentMethod.getPaymentMethodStatus(),
                        paymentMethod.getPaymentMethodTypeCode(),
                        paymentMethod.getPaymentMethodRanges(),
                        paymentMethod.getPaymentMethodAsset(),
                        paymentMethod.getClientIdEnum(),
                        new PaymentMethodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE_ONLY), // forcilbly set
                                                                                                      // onboardable
                                                                                                      // only to method
                                                                                                      // management
                        paymentMethod.getPaymentMethodBrandAsset()
                );
            }
        }
        return paymentMethod;
    }

    public Mono<PaymentMethod> updatePaymentMethodStatus(
                                                         String id,
                                                         PaymentMethodStatusEnum status
    ) {
        log.info("[Payment method Aggregate] Patch Aggregate");

        return paymentMethodRepository
                .findById(id)
                .map(this::docToAggregate)
                .switchIfEmpty(
                        Mono.error(new PaymentMethodNotFoundException(id))
                )
                .map(p -> {
                    p.setPaymentMethodStatus(status);
                    return p;
                })
                .flatMap(
                        p -> paymentMethodRepository
                                .save(
                                        new PaymentMethodDocument(
                                                p.getPaymentMethodID().value().toString(),
                                                p.getPaymentMethodName().value(),
                                                p.getPaymentMethodDescription().value(),
                                                p.getPaymentMethodStatus().value().toString(),
                                                p.getPaymentMethodAsset().value(),
                                                p.getPaymentMethodRanges().stream().map(
                                                        r -> Pair.of(r.min(), r.max())
                                                ).toList(),
                                                p.getPaymentMethodTypeCode().value(),
                                                p.getClientIdEnum().getValue(),
                                                p.getPaymentMethodManagement().value().getValue(),
                                                p.getPaymentMethodBrandAsset().brandAssets().orElse(null)
                                        )
                                )
                )
                .map(this::docToAggregate);
    }

    public Mono<PaymentMethod> retrievePaymentMethodById(
                                                         String id,
                                                         String clientId
    ) {
        log.info("[Payment Method Aggregate] Retrieve Aggregate");

        return paymentMethodRepository
                .findByPaymentMethodIDAndClientId(id, clientId)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(id)))
                .map(this::docToAggregate);
    }

    public Mono<CalculateFeeResponseDto> computeFee(
                                                    CalculateFeeRequestDto paymentOptionDto,
                                                    String paymentMethodId,
                                                    Integer maxOccurrences
    ) {
        log.info("[Payment Method] Retrieve bundles list");
        return paymentMethodRepository.findById(paymentMethodId)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(paymentMethodId)))
                .flatMap(
                        pm -> Mono.just(paymentOptionDto).map(
                                po -> Tuples.of(
                                        new it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto()
                                                .bin(po.getBin())
                                                .paymentAmount(po.getPaymentAmount())
                                                .idPspList(
                                                        Optional.ofNullable(po.getIdPspList()).orElseGet(ArrayList::new)
                                                                .stream()
                                                                .map(idPsp -> new PspSearchCriteriaDto().idPsp(idPsp))
                                                                .toList()
                                                )
                                                .paymentMethod(pm.getPaymentMethodTypeCode())
                                                .primaryCreditorInstitution(po.getPrimaryCreditorInstitution())
                                                .touchpoint(po.getTouchpoint())
                                                .transferList(
                                                        po.getTransferList()
                                                                .stream()
                                                                .map(
                                                                        t -> new TransferListItemDto()
                                                                                .creditorInstitution(
                                                                                        t.getCreditorInstitution()
                                                                                )
                                                                                .digitalStamp(t.getDigitalStamp())
                                                                                .transferCategory(
                                                                                        t.getTransferCategory()
                                                                                )
                                                                )
                                                                .toList()
                                                ),
                                        po.getIsAllCCP()
                                )

                        ).flatMap(tuple -> afmClient.getFees(tuple.getT1(), maxOccurrences, tuple.getT2()))
                                .map(BundleOptions::removeDuplicatePsp)
                                .map(bo -> bundleOptionToResponse(bo, pm))
                                .filter(response -> !response.getBundles().isEmpty())
                                .switchIfEmpty(
                                        Mono.error(
                                                new NoBundleFoundException(
                                                        paymentMethodId,
                                                        paymentOptionDto.getPaymentAmount(),
                                                        paymentOptionDto.getTouchpoint()
                                                )
                                        )
                                )

                );

    }

    public Mono<CreateSessionResponseDto> createSessionForPaymentMethod(
                                                                        String id,
                                                                        String language,
                                                                        ClientIdDto xClientId
    ) {
        log.info(
                "[Payment Method service] create new NPG sessions using paymentMethodId: {}",
                id
        );
        return paymentMethodRepository.findById(id)
                .map(PaymentMethodDocument::getPaymentMethodName)
                .map(NpgClient.PaymentMethod::fromServiceName)
                .flatMap(
                        paymentMethod -> uniqueIdUtils.generateUniqueId()
                                .map(orderId -> Tuples.of(orderId, paymentMethod))
                )
                .flatMap(
                        orderIdAndPaymentMethod -> jwtTokenIssuerClient.createJWTToken(
                                new CreateTokenRequestDto().privateClaims(
                                        Map.of(
                                                JwtIssuerClient.ORDER_ID_CLAIM,
                                                orderIdAndPaymentMethod.getT1(),
                                                JwtIssuerClient.PAYMENT_METHOD_ID_CLAIM,
                                                id
                                        )
                                ).audience(
                                        JwtIssuerClient.NPG_AUDIENCE
                                ).duration(npgNotificationTokenValidityTime)
                        ).flatMap(
                                token -> Mono.just(
                                        Tuples.of(
                                                orderIdAndPaymentMethod.getT1(),
                                                orderIdAndPaymentMethod.getT2(),
                                                token.getToken()
                                        )
                                )
                        )
                )
                .flatMap(data -> {
                    UUID correlationId = UUID.randomUUID();
                    log.info("Generated correlationId for execute NPG build session: {}", correlationId);
                    NpgClient.PaymentMethod paymentMethod = data.getT2();
                    String orderId = data.getT1();
                    String notificationSessionToken = data.getT3();
                    SessionPaymentMethod sessionPaymentMethod = SessionPaymentMethod
                            .fromValue(paymentMethod.serviceName);
                    URI returnUrlBasePath = ClientIdDto.IO.equals(xClientId) ? sessionUrlConfig.ioBasePath()
                            : sessionUrlConfig.basePath();

                    URI resultUrl = buildSessionOutcomeUrlWithClientPath(
                            returnUrlBasePath,
                            sessionUrlConfig.outcomeSuffix()
                    );

                    URI cancelUrl = buildSessionOutcomeUrlWithClientPath(
                            returnUrlBasePath,
                            sessionUrlConfig.cancelSuffix()
                    );

                    URI notificationUrl = UriComponentsBuilder
                            .fromUriString(sessionUrlConfig.notificationUrl())
                            .build(
                                    Map.of(
                                            "orderId",
                                            orderId,
                                            "sessionToken",
                                            notificationSessionToken
                                    )
                            );

                    return npgClient.buildForm(
                            correlationId, // correlationId
                            returnUrlBasePath, // merchantUrl
                            resultUrl, // resultUrl
                            notificationUrl, // notificationUrl
                            cancelUrl, // cancelUrl
                            orderId, // orderId
                            null, // customerId
                            paymentMethod, // paymentMethod
                            npgDefaultApiKey, // defaultApiKey
                            null, // contractId
                            language // language

                    ).map(form -> Tuples.of(form, sessionPaymentMethod, orderId, correlationId));
                }).flatMap(data -> {
                    FieldsDto fields = data.getT1();
                    String orderId = data.getT3();
                    UUID correlationId = data.getT4();
                    return npgSessionsTemplateWrapper
                            .save(
                                    new NpgSessionDocument(
                                            orderId,
                                            correlationId.toString(),
                                            fields.getSessionId(),
                                            fields.getSecurityToken(),
                                            null,
                                            null
                                    )
                            ).thenReturn(data);
                }).map(data -> {
                    FieldsDto fields = data.getT1();
                    SessionPaymentMethod paymentMethod = data.getT2();
                    String orderId = data.getT3();
                    UUID correlationId = data.getT4();
                    return new CreateSessionResponseDto()
                            .orderId(orderId)
                            .correlationId(correlationId)
                            .paymentMethodData(
                                    new CardFormFieldsDto()
                                            .paymentMethod(paymentMethod.value)
                                            .form(
                                                    fields.getFields()
                                                            .stream()
                                                            .map(
                                                                    field -> new FieldDto()
                                                                            .id(field.getId())
                                                                            .type(field.getType())
                                                                            .propertyClass(field.getPropertyClass())
                                                                            .src(URI.create(field.getSrc()))
                                                            )
                                                            .collect(Collectors.toList())
                                            )
                            );
                });
    }

    public Mono<SessionPaymentMethodResponseDto> getCardDataInformation(
                                                                        String id,
                                                                        String orderId
    ) {
        log.info(
                "[Payment Method service] Retrieve card data from NPG using paymentMethodId: {} and orderId: {}",
                id,
                orderId
        );
        return paymentMethodRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(id)))
                .flatMap(
                        el -> npgSessionsTemplateWrapper.findById(orderId)
                )
                .switchIfEmpty(Mono.error(new OrderIdNotFoundException(orderId)))
                .flatMap(
                        sx -> {
                            Mono<SessionPaymentMethodResponseDto> response;
                            if (sx.cardData() != null) {
                                log.info("Cache hit for orderId: {}", orderId);
                                response = Mono.just(
                                        new SessionPaymentMethodResponseDto()
                                                .bin(sx.cardData().bin())
                                                .sessionId(sx.sessionId())
                                                .brand(sx.cardData().circuit())
                                                .expiringDate(sx.cardData().expiringDate())
                                                .lastFourDigits(sx.cardData().lastFourDigits())
                                );
                            } else {
                                log.info("Cache miss for orderId: {}", orderId);
                                response = npgClient.getCardData(
                                        UUID.fromString(sx.correlationId()),
                                        sx.sessionId(),
                                        npgDefaultApiKey
                                )
                                        .flatMap(
                                                el -> npgSessionsTemplateWrapper.save(
                                                        new NpgSessionDocument(
                                                                sx.orderId(),
                                                                sx.correlationId(),
                                                                sx.sessionId(),
                                                                sx.securityToken(),
                                                                new CardDataDocument(
                                                                        el.getBin(),
                                                                        el.getLastFourDigits(),
                                                                        el.getExpiringDate(),
                                                                        el.getCircuit()
                                                                ),
                                                                null
                                                        )
                                                ).thenReturn(el)
                                        )
                                        .map(
                                                el -> new SessionPaymentMethodResponseDto().bin(el.getBin())
                                                        .sessionId(sx.sessionId())
                                                        .brand(el.getCircuit())
                                                        .expiringDate(el.getExpiringDate())
                                                        .lastFourDigits(el.getLastFourDigits())
                                        );
                            }
                            return response;
                        }
                );
    }

    public Mono<NpgSessionDocument> updateSession(
                                                  String paymentMethodId,
                                                  String orderId,
                                                  PatchSessionRequestDto updateData
    ) {
        return paymentMethodRepository.findById(paymentMethodId)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(paymentMethodId)))
                .flatMap(ignore -> npgSessionsTemplateWrapper.findById(orderId))
                .switchIfEmpty(Mono.error(new OrderIdNotFoundException(orderId)))
                .flatMap(document -> {
                    // Session associated to the order is associated to a different transaction id,
                    // not permitted
                    if (document.transactionId() != null
                            && !document.transactionId().equals(updateData.getTransactionId())) {
                        log.error(
                                "Session's transaction id ({}) differs from requested transaction id ({})",
                                document.transactionId(),
                                updateData.getTransactionId()
                        );
                        return Mono.error(
                                new SessionAlreadyAssociatedToTransaction(
                                        orderId,
                                        document.transactionId(),
                                        updateData.getTransactionId()
                                )
                        );
                    } else {
                        return Mono.just(document);
                    }
                })
                .flatMap(d -> {
                    // Transaction already associated to session, retry case
                    if (d.transactionId() != null) {
                        return Mono.just(d);
                    } else {
                        NpgSessionDocument updatedDocument = new NpgSessionDocument(
                                d.orderId(),
                                d.correlationId(),
                                d.sessionId(),
                                d.securityToken(),
                                d.cardData(),
                                updateData.getTransactionId()
                        );
                        return npgSessionsTemplateWrapper.save(updatedDocument).thenReturn(updatedDocument);
                    }
                });
    }

    private CalculateFeeResponseDto bundleOptionToResponse(
                                                           it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto bundle,
                                                           PaymentMethodDocument paymentMethodDocument
    ) {
        return new CalculateFeeResponseDto()
                .belowThreshold(bundle.getBelowThreshold())
                .paymentMethodName(paymentMethodDocument.getPaymentMethodName())
                .paymentMethodDescription(paymentMethodDocument.getPaymentMethodDescription())
                .paymentMethodStatus(PaymentMethodStatusDto.valueOf(paymentMethodDocument.getPaymentMethodStatus()))
                .bundles(
                        sortAndShuffleBundleList(
                                bundle.getBundleOptions() != null ? bundle.getBundleOptions()
                                        .stream()
                                        .map(
                                                t -> new BundleDto()
                                                        .abi(t.getAbi())
                                                        .bundleDescription(t.getBundleDescription())
                                                        .bundleName(t.getBundleName())
                                                        .idBrokerPsp(t.getIdBrokerPsp())
                                                        .idBundle(t.getIdBundle())
                                                        .idChannel(t.getIdChannel())
                                                        .idCiBundle(t.getIdCiBundle())
                                                        .idPsp(t.getIdPsp())
                                                        .onUs(t.getOnUs())
                                                        .paymentMethod(
                                                                // A null value is considered as "any" in the AFM domain
                                                                t.getPaymentMethod() == null
                                                                        ? paymentMethodDocument
                                                                                .getPaymentMethodTypeCode()
                                                                        : t.getPaymentMethod()
                                                        )
                                                        .primaryCiIncurredFee(t.getPrimaryCiIncurredFee())
                                                        .taxPayerFee(t.getTaxPayerFee())
                                                        .touchpoint(t.getTouchpoint())
                                                        .pspBusinessName(t.getPspBusinessName())
                                        ).toList() : new ArrayList<>()
                        )
                )
                .asset(paymentMethodDocument.getPaymentMethodAsset())
                .brandAssets(paymentMethodDocument.getPaymentMethodsBrandAssets());
    }

    private PaymentMethod docToAggregate(PaymentMethodDocument doc) {
        if (doc == null) {
            return null;
        }

        return new PaymentMethod(
                new PaymentMethodID(UUID.fromString(doc.getPaymentMethodID())),
                new PaymentMethodName(doc.getPaymentMethodName()),
                new PaymentMethodDescription(doc.getPaymentMethodDescription()),
                new PaymentMethodStatus(PaymentMethodStatusEnum.valueOf(doc.getPaymentMethodStatus())),
                new PaymentMethodType(doc.getPaymentMethodTypeCode()),
                doc.getPaymentMethodRanges().stream()
                        .map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond()))
                        .toList(),
                new PaymentMethodAsset(doc.getPaymentMethodAsset()),
                PaymentMethodRequestDto.ClientIdEnum.fromValue(doc.getClientId()),
                new PaymentMethodManagement(
                        PaymentMethodManagementTypeDto.valueOf(doc.getMethodManagement())
                ),
                new PaymentMethodBrandAssets(Optional.ofNullable(doc.getPaymentMethodsBrandAssets()))
        );
    }

    private List<it.pagopa.ecommerce.payment.methods.server.model.BundleDto> sortAndShuffleBundleList(
                                                                                                      List<it.pagopa.ecommerce.payment.methods.server.model.BundleDto> bundles
    ) {
        Map<Long, List<it.pagopa.ecommerce.payment.methods.server.model.BundleDto>> bundleMap = new TreeMap<>();
        Optional<it.pagopa.ecommerce.payment.methods.server.model.BundleDto> onUsBundle = bundles
                .stream()
                .filter(it.pagopa.ecommerce.payment.methods.server.model.BundleDto::getOnUs)
                .findFirst();
        bundles
                .stream()
                .filter(Predicate.not(it.pagopa.ecommerce.payment.methods.server.model.BundleDto::getOnUs))
                .forEach(bundle -> {
                    Long fees = bundle.getTaxPayerFee();
                    List<it.pagopa.ecommerce.payment.methods.server.model.BundleDto> bundlesPerFee = bundleMap
                            .getOrDefault(fees, new ArrayList<>());
                    bundlesPerFee.add(bundle);
                    bundleMap.put(fees, bundlesPerFee);
                });
        Deque<it.pagopa.ecommerce.payment.methods.server.model.BundleDto> orderedBundles = new LinkedList<>();
        bundleMap.values().forEach(bundlesPerFee -> {
            Collections.shuffle(bundlesPerFee);
            orderedBundles.addAll(bundlesPerFee);
        });
        onUsBundle.ifPresent(orderedBundles::addFirst);
        return orderedBundles.stream().toList();
    }

    /**
     * Build the outcome (success or cancel) URL to pass to NPG when creating a
     * session for a given client ID
     *
     * @param basePath the base path
     * @param suffix   the wanted suffix
     * @return URI
     */
    private URI buildSessionOutcomeUrlWithClientPath(
                                                     URI basePath,
                                                     String suffix
    ) {
        return UriComponentsBuilder
                .fromUri(basePath)
                .path(suffix)
                .queryParam("t", Instant.now().toEpochMilli())
                .build()
                .toUri();

    }
}
