package it.pagopa.ecommerce.payment.methods.application.v2;

import io.vavr.Tuple;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.domain.Claims;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import it.pagopa.ecommerce.commons.utils.JwtTokenUtils;
import it.pagopa.ecommerce.commons.utils.UniqueIdUtils;
import it.pagopa.ecommerce.payment.methods.application.BundleOptions;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.ecommerce.payment.methods.v2.server.model.BundleDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeRequestDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeResponseDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.PaymentMethodStatusDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.PaymentNoticeDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.PaymentNoticeItemDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.PaymentOptionMultiDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.PspSearchCriteriaDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.TransferListItemDto;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import javax.crypto.SecretKey;

@Service(PaymentMethodService.QUALIFIER_NAME)
@ApplicationService
@Slf4j
public class PaymentMethodService {

    protected static final String QUALIFIER_NAME = "paymentMethodServiceV2";

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

    public Mono<CreateSessionResponseDto> createSessionForPaymentMethod(
                                                                        String id,
                                                                        String language
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
                                new Claims(null, orderIdAndPaymentMethod.getT1(), id, null)
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
                    it.pagopa.ecommerce.payment.methods.application.v1.PaymentMethodService.SessionPaymentMethod sessionPaymentMethod = it.pagopa.ecommerce.payment.methods.application.v1.PaymentMethodService.SessionPaymentMethod
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
                            npgDefaultApiKey, // defaultApiKey
                            null,
                            language

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
                    it.pagopa.ecommerce.payment.methods.application.v1.PaymentMethodService.SessionPaymentMethod paymentMethod = data
                            .getT2();
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

    public Mono<CalculateFeeResponseDto> computeFee(
                                                    CalculateFeeRequestDto feeRequestDto,
                                                    String paymentMethodId,
                                                    Integer maxOccurrences
    ) {
        log.info(
                "[Payment Method] Retrieve bundles list for payment method: [{}], allCcp: [{}], isMulti: [{}] and payment notice amounts: {}",
                paymentMethodId,
                feeRequestDto.getIsAllCCP(),
                feeRequestDto.getPaymentNotices().size() > 1,
                feeRequestDto.getPaymentNotices().stream().map(PaymentNoticeDto::getPaymentAmount).toList()
        );
        return paymentMethodRepository.findById(paymentMethodId)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(paymentMethodId)))
                .flatMap(
                        paymentMethod -> afmClient.getFeesForNotices(
                                createGecFeeRequest(paymentMethod, feeRequestDto),
                                maxOccurrences,
                                feeRequestDto.getIsAllCCP()
                        ).map(bundle -> Tuple.of(paymentMethod, bundle))
                )
                .map(bundleAndPaymentMethod -> bundleAndPaymentMethod.map2(BundleOptions::removeDuplicatePspV2))
                .map(
                        bundleAndPaymentMethod -> bundleOptionToResponse(
                                bundleAndPaymentMethod._2(),
                                bundleAndPaymentMethod._1()
                        )
                )
                .filter(response -> !response.getBundles().isEmpty())
                .switchIfEmpty(
                        Mono.error(
                                new NoBundleFoundException(
                                        paymentMethodId,
                                        feeRequestDto.getPaymentNotices().stream()
                                                .map(PaymentNoticeDto::getPaymentAmount).reduce(Long::sum).orElse(0L),
                                        feeRequestDto.getTouchpoint()
                                )
                        )
                )
                .doOnError(
                        NoBundleFoundException.class,
                        error -> log
                                .error(String.format("No bundle found for payment method [%s]", paymentMethodId), error)
                );
    }

    private PaymentOptionMultiDto createGecFeeRequest(
                                                      PaymentMethodDocument paymentMethod,
                                                      CalculateFeeRequestDto feeRequestDto
    ) {
        final var paymentNotices = feeRequestDto.getPaymentNotices().stream()
                .map(
                        it -> new PaymentNoticeItemDto()
                                .paymentAmount(it.getPaymentAmount())
                                .primaryCreditorInstitution(it.getPrimaryCreditorInstitution())
                                .transferList(
                                        it.getTransferList()
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
                                                ).toList()
                                )
                )
                .toList();

        return new PaymentOptionMultiDto()
                .bin(feeRequestDto.getBin())
                .idPspList(
                        Optional.ofNullable(feeRequestDto.getIdPspList()).orElseGet(
                                ArrayList::new
                        )
                                .stream()
                                .map(idPsp -> new PspSearchCriteriaDto().idPsp(idPsp))
                                .toList()
                )
                .paymentMethod(paymentMethod.getPaymentMethodTypeCode())
                .touchpoint(feeRequestDto.getTouchpoint())
                .paymentNotice(paymentNotices);
    }

    private CalculateFeeResponseDto bundleOptionToResponse(
                                                           it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto bundle,
                                                           PaymentMethodDocument paymentMethodDocument
    ) {
        final var bundles = Optional.ofNullable(bundle.getBundleOptions())
                .orElse(List.of())
                .stream()
                .map(
                        t -> new BundleDto()
                                .abi(t.getAbi())
                                .bundleDescription(t.getBundleDescription())
                                .bundleName(t.getBundleName())
                                .idBrokerPsp(t.getIdBrokerPsp())
                                .idBundle(t.getIdBundle())
                                .idChannel(t.getIdChannel())
                                .idPsp(t.getIdPsp())
                                .onUs(t.getOnUs())
                                .paymentMethod(
                                        // A null value is considered as "any" in the AFM domain
                                        Optional.ofNullable(t.getPaymentMethod())
                                                .orElse(paymentMethodDocument.getPaymentMethodTypeCode())
                                )
                                .taxPayerFee(t.getTaxPayerFee())
                                .touchpoint(t.getTouchpoint())
                                .pspBusinessName(t.getPspBusinessName())
                ).toList();

        return new CalculateFeeResponseDto()
                .belowThreshold(bundle.getBelowThreshold())
                .paymentMethodName(paymentMethodDocument.getPaymentMethodName())
                .paymentMethodDescription(paymentMethodDocument.getPaymentMethodDescription())
                .paymentMethodStatus(PaymentMethodStatusDto.valueOf(paymentMethodDocument.getPaymentMethodStatus()))
                .bundles(bundles)
                .asset(paymentMethodDocument.getPaymentMethodAsset())
                .brandAssets(paymentMethodDocument.getPaymentMethodsBrandAssets());
    }
}
