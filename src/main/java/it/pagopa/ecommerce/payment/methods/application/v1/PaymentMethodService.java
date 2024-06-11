package it.pagopa.ecommerce.payment.methods.application.v1;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.domain.Claims;
import it.pagopa.ecommerce.commons.domain.TransactionId;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import it.pagopa.ecommerce.commons.utils.JwtTokenUtils;
import it.pagopa.ecommerce.commons.utils.UniqueIdUtils;
import it.pagopa.ecommerce.payment.methods.application.BundleOptions;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodAsset;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodBrandAssets;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodID;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodManagement;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodRange;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodStatus;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodType;
import it.pagopa.ecommerce.payment.methods.exception.InvalidSessionException;
import it.pagopa.ecommerce.payment.methods.exception.MismatchedSecurityTokenException;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.exception.OrderIdNotFoundException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.exception.SessionAlreadyAssociatedToTransaction;
import it.pagopa.ecommerce.payment.methods.infrastructure.CardDataDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.BundleDto;
import it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeResponseDto;
import it.pagopa.ecommerce.payment.methods.server.model.CardFormFieldsDto;
import it.pagopa.ecommerce.payment.methods.server.model.CreateSessionResponseDto;
import it.pagopa.ecommerce.payment.methods.server.model.FieldDto;
import it.pagopa.ecommerce.payment.methods.server.model.PatchSessionRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodManagementTypeDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodStatusDto;
import it.pagopa.ecommerce.payment.methods.server.model.SessionPaymentMethodResponseDto;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.generated.ecommerce.gec.v1.dto.PspSearchCriteriaDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferListItemDto;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service(PaymentMethodService.QUALIFIER_NAME)
@ApplicationService
@Slf4j
public class PaymentMethodService {

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

    private final UniqueIdUtils uniqueIdUtils;

    private final SecretKey npgJwtSigningKey;

    private final int npgNotificationTokenValidityTime;

    private final JwtTokenUtils jwtTokenUtils;

    @Autowired
    public PaymentMethodService(
            AfmClient afmClient,
            PaymentMethodRepository paymentMethodRepository,
            PaymentMethodFactory paymentMethodFactory,
            NpgClient npgClient,
            SessionUrlConfig sessionUrlConfig,
            NpgSessionsTemplateWrapper npgSessionsTemplateWrapper,
            @Value("${npg.client.apiKey}") String npgDefaultApiKey,
            UniqueIdUtils uniqueIdUtils,
            SecretKey npgJwtSigningKey,
            @Value("${npg.notification.jwt.validity.time}") int npgNotificationTokenValidityTime,
            JwtTokenUtils jwtTokenUtils
    ) {
        this.afmClient = afmClient;
        this.npgClient = npgClient;
        this.paymentMethodFactory = paymentMethodFactory;
        this.paymentMethodRepository = paymentMethodRepository;
        this.sessionUrlConfig = sessionUrlConfig;
        this.npgSessionsTemplateWrapper = npgSessionsTemplateWrapper;
        this.npgDefaultApiKey = npgDefaultApiKey;
        this.uniqueIdUtils = uniqueIdUtils;
        this.npgJwtSigningKey = npgJwtSigningKey;
        this.npgNotificationTokenValidityTime = npgNotificationTokenValidityTime;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    public Mono<PaymentMethod> createPaymentMethod(
                                                   PaymentMethodRequestDto paymentMethodRequestDto

    ) {
        String paymentMethodName = paymentMethodRequestDto.getName();
        String paymentMethodDescription = paymentMethodRequestDto.getDescription();
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
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
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
                                                      String clientId
    ) {
        log.info("[Payment Method Aggregate] Retrieve Aggregate");

        return paymentMethodRepository.findByClientId(clientId).filter(
                doc -> amount == null || doc.getPaymentMethodRanges().stream()
                        .anyMatch(
                                range -> range.getFirst().longValue() <= amount
                                        && range.getSecond().longValue() >= amount
                        )
        ).sort(
                (
                 o1,
                 o2
                ) -> {
                    if (o1.getPaymentMethodTypeCode().equals("CP"))
                        return -1;
                    if (o2.getPaymentMethodTypeCode().equals("CP"))
                        return 1;
                    else
                        return o1.getPaymentMethodDescription()
                                .compareTo(o2.getPaymentMethodDescription());
                }
        ).map(this::docToAggregate);
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
                                                                        String id
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
                        orderIdAndPaymentMethod -> jwtTokenUtils.generateToken(
                                npgJwtSigningKey,
                                npgNotificationTokenValidityTime,
                                new Claims(null, orderIdAndPaymentMethod.getT1(), id)
                        ).fold(
                                Mono::error,
                                token -> Mono.just(
                                        Tuples.of(
                                                orderIdAndPaymentMethod.getT1(),
                                                orderIdAndPaymentMethod.getT2(),
                                                token
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
                    URI returnUrlBasePath = sessionUrlConfig.basePath();
                    URI resultUrl = returnUrlBasePath.resolve(sessionUrlConfig.outcomeSuffix());
                    URI merchantUrl = returnUrlBasePath;
                    URI cancelUrl = returnUrlBasePath.resolve(sessionUrlConfig.cancelSuffix());
                    URI notificationUrl = UriComponentsBuilder
                            .fromHttpUrl(sessionUrlConfig.notificationUrl())
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
                            merchantUrl, // merchantUrl
                            resultUrl, // resultUrl
                            notificationUrl, // notificationUrl
                            cancelUrl, // cancelUrl
                            orderId, // orderId
                            null, // customerId
                            paymentMethod, // paymentMethod
                            npgDefaultApiKey // defaultApiKey

                    ).map(form -> Tuples.of(form, sessionPaymentMethod, orderId, correlationId));
                }).map(data -> {
                    FieldsDto fields = data.getT1();
                    String orderId = data.getT3();
                    UUID correlationId = data.getT4();
                    npgSessionsTemplateWrapper
                            .save(
                                    new NpgSessionDocument(
                                            orderId,
                                            correlationId.toString(),
                                            fields.getSessionId(),
                                            fields.getSecurityToken(),
                                            null,
                                            null
                                    )
                            );
                    return data;
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
                .map(
                        el -> npgSessionsTemplateWrapper.findById(orderId)
                )
                .flatMap(
                        session -> session.map(
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
                                                .doOnSuccess(
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
                                                        )
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

                        ).orElse(
                                Mono.error(new OrderIdNotFoundException(orderId))
                        )
                );
    }

    public Mono<String> isSessionValid(
                                       String paymentMethodId,
                                       String orderId,
                                       String securityToken
    ) {
        return paymentMethodRepository
                .findById(paymentMethodId)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(paymentMethodId)))
                .doOnError(e -> log.info("Error while looking for payment method with id {}: ", paymentMethodId, e))
                .map(
                        ignore -> npgSessionsTemplateWrapper.findById(orderId)
                )
                .doOnNext(doc -> log.info("Found session for order id {}: {}", orderId, doc.isPresent()))
                .flatMap(doc -> doc.map(Mono::just).orElse(Mono.error(new OrderIdNotFoundException(orderId))))
                .flatMap(doc -> {
                    String transactionId = doc.transactionId();
                    if (transactionId == null) {
                        return Mono.error(new InvalidSessionException(orderId));
                    } else {
                        return Mono.just(doc);
                    }
                })
                .flatMap(doc -> {
                    if (!doc.securityToken().equals(securityToken)) {
                        log.warn("Invalid security token for requested order id {}", orderId);
                        return Mono.error(new MismatchedSecurityTokenException(orderId, doc.transactionId()));
                    } else {
                        return Mono.just(doc);
                    }
                })
                .mapNotNull(NpgSessionDocument::transactionId)
                .map(TransactionId::new)
                .map(TransactionId::base64);
    }

    public Mono<NpgSessionDocument> updateSession(
                                                  String paymentMethodId,
                                                  String orderId,
                                                  PatchSessionRequestDto updateData
    ) {
        return paymentMethodRepository.findById(paymentMethodId)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(paymentMethodId)))
                .map(ignore -> npgSessionsTemplateWrapper.findById(orderId))
                .flatMap(document -> document.map(Mono::just).orElse(Mono.empty()))
                .switchIfEmpty(Mono.error(new OrderIdNotFoundException(orderId)))
                .flatMap(document -> {
                    if (document.transactionId() != null) {
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
                .map(d -> {
                    NpgSessionDocument updatedDocument = new NpgSessionDocument(
                            d.orderId(),
                            d.correlationId(),
                            d.sessionId(),
                            d.securityToken(),
                            d.cardData(),
                            updateData.getTransactionId()
                    );
                    npgSessionsTemplateWrapper.save(updatedDocument);

                    return updatedDocument;
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
                                                                ? paymentMethodDocument.getPaymentMethodTypeCode()
                                                                : t.getPaymentMethod()
                                                )
                                                .primaryCiIncurredFee(t.getPrimaryCiIncurredFee())
                                                .taxPayerFee(t.getTaxPayerFee())
                                                .touchpoint(t.getTouchpoint())
                                                .pspBusinessName(t.getPspBusinessName())
                                ).toList() : new ArrayList<>()
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
}
