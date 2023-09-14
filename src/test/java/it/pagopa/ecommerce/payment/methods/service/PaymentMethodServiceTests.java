package it.pagopa.ecommerce.payment.methods.service;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.domain.v1.TransactionId;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.CardDataResponseDto;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.exception.InvalidSessionException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.exception.SessionAlreadyAssociatedToTransaction;
import it.pagopa.ecommerce.payment.methods.exception.SessionIdNotFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTests {

    private final AfmClient afmClient = mock(AfmClient.class);

    private final NpgClient npgClient = mock(NpgClient.class);

    private final PaymentMethodRepository paymentMethodRepository = mock(PaymentMethodRepository.class);

    private final PaymentMethodFactory paymentMethodFactory = mock(PaymentMethodFactory.class);

    private final SessionUrlConfig sessionUrlConfig = new SessionUrlConfig(
            URI.create("http://localhost:1234"),
            "/esito",
            "/annulla",
            "https://localhost/sessions/{orderId}/outcomes?paymentMethodId={paymentMethodId}"
    );

    private final String npgDefaultApiKey = UUID.randomUUID().toString();

    private final NpgSessionsTemplateWrapper npgSessionsTemplateWrapper = mock(NpgSessionsTemplateWrapper.class);

    private final PaymentMethodService paymentMethodService = new PaymentMethodService(
            afmClient,
            paymentMethodRepository,
            paymentMethodFactory,
            npgClient,
            sessionUrlConfig,
            npgSessionsTemplateWrapper,
            npgDefaultApiKey
    );

    @Test
    void shouldCreatePaymentMethod() {
        Hooks.onOperatorDebug();

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        Mockito.when(
                paymentMethodFactory.newPaymentMethod(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                )
        )
                .thenReturn(Mono.just(paymentMethod));

        Mockito.when(
                paymentMethodRepository.save(
                        paymentMethodDocument
                )
        )
                .thenReturn(Mono.just(paymentMethodDocument));

        PaymentMethod paymentMethodResponse = paymentMethodService.createPaymentMethod(
                paymentMethod.getPaymentMethodName().value(),
                paymentMethod.getPaymentMethodDescription().value(),
                paymentMethod.getPaymentMethodRanges().stream().map(r -> Pair.of(r.min(), r.max()))
                        .collect(Collectors.toList()),
                paymentMethod.getPaymentMethodTypeCode().value(),
                paymentMethod.getPaymentMethodAsset().value()
        ).block();

        assertEquals(paymentMethodResponse.getPaymentMethodID(), paymentMethod.paymentMethodID());
    }

    @Test
    void shouldRetrievePaymentMethods() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        Mockito.when(paymentMethodRepository.findAll())
                .thenReturn(Flux.just(paymentMethodDocument));

        PaymentMethod paymentMethodCreated = paymentMethodService.retrievePaymentMethods(null).blockFirst();

        assertEquals(paymentMethodCreated.getPaymentMethodID(), paymentMethod.getPaymentMethodID());
    }

    @Test
    void shouldNotRetrievePaymentMethodsWithAmount() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        Mockito.when(paymentMethodRepository.findAll())
                .thenReturn(Flux.just(paymentMethodDocument));

        List<PaymentMethod> paymentMethodCreated = paymentMethodService.retrievePaymentMethods(101)
                .collectList().block();

        assertEquals(0, paymentMethodCreated.size());
    }

    @Test
    void shouldRetrievePaymentMethodsWithAmount() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        Mockito.when(paymentMethodRepository.findAll())
                .thenReturn(Flux.just(paymentMethodDocument));

        List<PaymentMethod> paymentmethodCreated = paymentMethodService.retrievePaymentMethods(50)
                .collectList().block();

        assertEquals(1, paymentmethodCreated.size());
    }

    @Test
    void shouldPatchPaymentMethod() {

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        paymentMethodDocument.setPaymentMethodStatus(PaymentMethodStatusEnum.DISABLED.getCode());
        Mockito.when(paymentMethodRepository.findById(paymentMethod.getPaymentMethodID().value().toString()))
                .thenReturn(
                        Mono.just(TestUtil.getTestPaymentDoc(paymentMethod))
                );

        Mockito.when(
                paymentMethodRepository.save(
                        paymentMethodDocument
                )
        )
                .thenReturn(Mono.just(paymentMethodDocument));

        PaymentMethod paymentMethodPatched = paymentMethodService
                .updatePaymentMethodStatus(
                        paymentMethod.getPaymentMethodID().value().toString(),
                        PaymentMethodStatusEnum.DISABLED
                )
                .block();

        assertEquals(
                paymentMethodPatched.getPaymentMethodID(),
                paymentMethod.getPaymentMethodID()
        );

        assertEquals(PaymentMethodStatusEnum.DISABLED, paymentMethodPatched.getPaymentMethodStatus().value());
    }

    @Test
    void shouldRetrievePaymentMethodById() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        Mockito.when(paymentMethodRepository.findById(paymentMethod.getPaymentMethodID().value().toString()))
                .thenReturn(Mono.just(paymentMethodDocument));

        PaymentMethod paymentMethodCreated = paymentMethodService
                .retrievePaymentMethodById(paymentMethod.getPaymentMethodID().value().toString()).block();

        assertEquals(paymentMethodCreated.getPaymentMethodID(), paymentMethod.getPaymentMethodID());
    }

    @Test
    void shouldRetrieveFee() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.getCalculateFeeRequest();
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();
        PaymentMethodDocument paymentMethodDocument = new PaymentMethodDocument(
                UUID.randomUUID().toString(),
                NpgClient.PaymentMethod.CARDS.serviceName,
                "Description",
                PaymentMethodStatusEnum.ENABLED.getCode(),
                "asset",
                List.of(Pair.of(0L, 100L)),
                "CP"
        );
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(
                        Mono.just(
                                paymentMethodDocument
                        )
                );
        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(Mono.just(calculateFeeRequestDto), paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(paymentMethodDocument.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                paymentMethodDocument.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
    }

    @Test
    void shouldRetrieveFeeWithoutPspList() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.getCalculateFeeRequest();
        calculateFeeRequestDto.setIdPspList(null);
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(
                        Mono.just(
                                new PaymentMethodDocument(
                                        UUID.randomUUID().toString(),
                                        NpgClient.PaymentMethod.CARDS.serviceName,
                                        "",
                                        PaymentMethodStatusEnum.ENABLED.getCode(),
                                        "asset",
                                        List.of(Pair.of(0L, 100L)),
                                        "CP"
                                )
                        )
                );

        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(Mono.just(calculateFeeRequestDto), paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
    }

    @Test
    void shouldRetrieveFeeWithPspWithNullPaymentType() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.getCalculateFeeRequest();
        calculateFeeRequestDto.setIdPspList(null);
        BundleOptionDto gecResponse = TestUtil.getBundleOptionWithAnyValueDtoClientResponse();
        String paymentTypeCode = "CP";

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(
                        Mono.just(
                                new PaymentMethodDocument(
                                        UUID.randomUUID().toString(),
                                        NpgClient.PaymentMethod.CARDS.serviceName,
                                        "",
                                        PaymentMethodStatusEnum.ENABLED.getCode(),
                                        "asset",
                                        List.of(Pair.of(0L, 100L)),
                                        paymentTypeCode
                                )
                        )
                );

        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(Mono.just(calculateFeeRequestDto), paymentMethodId, null).block();
        assertEquals(paymentTypeCode, serviceResponse.getBundles().get(0).getPaymentMethod());
    }

    @Test
    void shouldCreateSessionForValidPaymentMethod() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();
        FieldsDto npgResponse = TestUtil.npgResponse();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgClient.buildForm(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(
                        Mono.just(npgResponse)
                );
        Mockito.doNothing().when(npgSessionsTemplateWrapper).save(any());

        CreateSessionResponseDto expected = new CreateSessionResponseDto()
                .sessionId(npgResponse.getSessionId())
                .paymentMethodData(
                        new CardFormFieldsDto()
                                .paymentMethod(PaymentMethodService.SessionPaymentMethod.CARDS.value)
                                .form(
                                        npgResponse.getFields().stream().map(
                                                field -> new FieldDto()
                                                        .id(field.getId())
                                                        .type(field.getType())
                                                        .propertyClass(field.getPropertyClass())
                                                        .src(URI.create(field.getSrc()))
                                        )
                                                .collect(Collectors.toList())
                                )
                );

        StepVerifier.create(paymentMethodService.createSessionForPaymentMethod(paymentMethodId))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldRetrieveCardDataForInvalidPaymentMethodId() {
        String paymentMethodId = "paymentMethodId";
        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.empty());
        StepVerifier.create(paymentMethodService.getCardDataInformation(paymentMethodId, any()))
                .expectErrorMatches(e -> e instanceof PaymentMethodNotFoundException)
                .verify();

    }

    @Test
    void shouldReturnErrorForInvalidSessionId() {
        String paymentMethodId = "paymentMethodId";
        String sessionId = "sessionId";
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.empty());
        StepVerifier.create(paymentMethodService.getCardDataInformation(paymentMethodId, any()))
                .expectErrorMatches(e -> e instanceof SessionIdNotFoundException)
                .verify();

        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).findById(any());
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(0)).save(any());
        Mockito.verify(npgClient, Mockito.times(0)).getCardData(any(), any(), any());
    }

    @Test
    void shouldRetrieveCardDataWithCacheMiss() {
        String paymentMethodId = "paymentMethodId";
        String sessionId = "sessionId";
        CardDataResponseDto npgResponse = TestUtil.npgCardDataResponse();
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        SessionPaymentMethodResponseDto expectedResponse = new SessionPaymentMethodResponseDto()
                .bin(npgResponse.getBin()).sessionId(sessionId).expiringDate(npgResponse.getExpiringDate())
                .lastFourDigits(npgResponse.getLastFourDigits())
                .brand(npgResponse.getCircuit());
        NpgSessionDocument npgSessionDocument = TestUtil.npgSessionDocument(sessionId, false, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.of(npgSessionDocument));
        Mockito.when(npgClient.getCardData(any(), any(), any())).thenReturn(Mono.just(npgResponse));
        /* Tests */
        StepVerifier.create(paymentMethodService.getCardDataInformation(paymentMethodId, sessionId))
                .expectNext(expectedResponse)
                .verifyComplete();
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).findById(any());
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).save(any());
        Mockito.verify(npgClient, Mockito.times(1)).getCardData(any(), any(), any());
    }

    @Test
    void shouldRetrieveCardDataWithCacheHit() {
        String paymentMethodId = "paymentMethodId";
        String sessionId = "sessionId";
        CardDataResponseDto npgResponse = TestUtil.npgCardDataResponse();
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        SessionPaymentMethodResponseDto expectedResponse = new SessionPaymentMethodResponseDto()
                .bin(npgResponse.getBin()).sessionId(sessionId).expiringDate(npgResponse.getExpiringDate())
                .lastFourDigits(npgResponse.getLastFourDigits())
                .brand(npgResponse.getCircuit());
        NpgSessionDocument npgSessionDocument = TestUtil.npgSessionDocument(sessionId, true, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.of(npgSessionDocument));

        /* Tests */
        StepVerifier.create(paymentMethodService.getCardDataInformation(paymentMethodId, sessionId))
                .expectNext(expectedResponse)
                .verifyComplete();
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).findById(any());
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(0)).save(any());
        Mockito.verify(npgClient, Mockito.times(0)).getCardData(any(), any(), any());
    }

    @Test
    void shouldReturnTransactionIdForValidSession() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        NpgSessionDocument npgSessionDocument = TestUtil.npgSessionDocument("sessionId", false, transactionId.value());
        String encodedTransactionId = transactionId.base64();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(TestUtil.getTestPaymentDoc(paymentMethod)));
        Mockito.when(npgSessionsTemplateWrapper.findById(any())).thenReturn(Optional.of(npgSessionDocument));

        StepVerifier
                .create(
                        paymentMethodService.isSessionValid(
                                paymentMethodId,
                                npgSessionDocument.sessionId(),
                                npgSessionDocument.securityToken()
                        )
                )
                .expectNext(encodedTransactionId)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorForInvalidSession() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();
        NpgSessionDocument npgSessionDocument = TestUtil.npgSessionDocument("sessionId", false, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(TestUtil.getTestPaymentDoc(paymentMethod)));
        Mockito.when(npgSessionsTemplateWrapper.findById(any())).thenReturn(Optional.of(npgSessionDocument));

        StepVerifier
                .create(
                        paymentMethodService
                                .isSessionValid(paymentMethodId, npgSessionDocument.sessionId(), "OTHER_SECURITY_TOKEN")
                )
                .expectError(InvalidSessionException.class)
                .verify();
    }

    @Test
    void shouldReturnErrorForSessionNotFound() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(TestUtil.getTestPaymentDoc(paymentMethod)));
        Mockito.when(npgSessionsTemplateWrapper.findById(any())).thenReturn(Optional.empty());

        StepVerifier
                .create(
                        paymentMethodService
                                .isSessionValid(paymentMethodId, "NON_EXISTING_SESSION_ID", "SECURITY_TOKEN")
                )
                .expectError(SessionIdNotFoundException.class)
                .verify();
    }

    @Test
    void shouldReturnErrorForNonExistingMethod() {
        Mockito.when(paymentMethodRepository.findById(anyString())).thenReturn(Mono.empty());

        StepVerifier
                .create(
                        paymentMethodService
                                .isSessionValid("NON_EXISTING_METHOD_ID", "NON_EXISTING_SESSION_ID", "SECURITY_TOKEN")
                )
                .expectError(PaymentMethodNotFoundException.class)
                .verify();
    }

    @Test
    void shouldUpdateSessionData() {
        String sessionId = "sessionId";
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);
        NpgSessionDocument npgSessionDocument = TestUtil.npgSessionDocument(sessionId, true, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.of(npgSessionDocument));

        NpgSessionDocument expectedResponse = new NpgSessionDocument(
                npgSessionDocument.sessionId(),
                npgSessionDocument.securityToken(),
                npgSessionDocument.cardData(),
                patchSessionRequestDto.getTransactionId()
        );

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, sessionId, patchSessionRequestDto))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorOnSessionAlreadyAssociatedToTransactionId() {
        String sessionId = "sessionId";
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);
        NpgSessionDocument npgSessionDocument = TestUtil.npgSessionDocument(sessionId, true, "OTHER_TRANSACTION_ID");

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.of(npgSessionDocument));

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, sessionId, patchSessionRequestDto))
                .expectError(SessionAlreadyAssociatedToTransaction.class)
                .verify();
    }

    @Test
    void shouldReturnErrorOnNonExistingSession() {
        String sessionId = "sessionId";
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.empty());

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, sessionId, patchSessionRequestDto))
                .expectError(SessionIdNotFoundException.class)
                .verify();
    }

    @Test
    void shouldReturnErrorOnNonExistingPaymentMethod() {
        String sessionId = "sessionId";
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.empty());

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, sessionId, patchSessionRequestDto))
                .expectError(PaymentMethodNotFoundException.class)
                .verify();
    }
}
