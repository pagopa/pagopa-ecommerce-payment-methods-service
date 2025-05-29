package it.pagopa.ecommerce.payment.methods.service.v1;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.domain.v2.Claims;
import it.pagopa.ecommerce.commons.domain.v2.TransactionId;
import it.pagopa.ecommerce.commons.exceptions.JWTTokenGenerationException;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.CardDataResponseDto;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import it.pagopa.ecommerce.commons.utils.v2.JwtTokenUtils;
import it.pagopa.ecommerce.commons.utils.UniqueIdUtils;
import it.pagopa.ecommerce.payment.methods.application.v1.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.config.SecretsConfigurations;
import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.exception.*;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mongodb.assertions.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTests {
    @Mock
    private JwtTokenUtils jwtTokenUtils;

    private static final String STRONG_KEY = "ODMzNUZBNTZENDg3NTYyREUyNDhGNDdCRUZDNzI3NDMzMzQwNTFEREZGQ0MyQzA5Mjc1RjY2NTQ1NDk5MDMxNzU5NDc0NUVFMTdDMDhGNzk4Q0Q3RENFMEJBODE1NURDREExNEY2Mzk4QzFEMTU0NTExNjUyMEExMzMwMTdDMDk";

    private final AfmClient afmClient = mock(AfmClient.class);

    private final NpgClient npgClient = mock(NpgClient.class);

    private final PaymentMethodRepository paymentMethodRepository = mock(PaymentMethodRepository.class);

    private final PaymentMethodFactory paymentMethodFactory = mock(PaymentMethodFactory.class);

    private final SessionUrlConfig sessionUrlConfig = new SessionUrlConfig(
            URI.create("http://localhost:1234"),
            "/esito",
            "/annulla",
            "https://localhost/sessions/{orderId}/outcomes?sessionToken={sessionToken}"
    );

    private final String npgDefaultApiKey = UUID.randomUUID().toString();

    private final NpgSessionsTemplateWrapper npgSessionsTemplateWrapper = mock(NpgSessionsTemplateWrapper.class);

    private final UniqueIdUtils uniqueIdUtils = mock(UniqueIdUtils.class);

    private final SecretKey jwtSecretKey = new SecretsConfigurations().npgJwtSigningKey(STRONG_KEY);

    // private final JwtTokenUtils jwtTokenUtils = mock(JwtTokenUtils.class);
    private PaymentMethodService paymentMethodService;/*
                                                       * = new PaymentMethodService( afmClient, paymentMethodRepository,
                                                       * paymentMethodFactory, npgClient, sessionUrlConfig,
                                                       * npgSessionsTemplateWrapper, npgDefaultApiKey, uniqueIdUtils,
                                                       * jwtSecretKey, 900, jwtTokenUtils );
                                                       */

    @BeforeEach
    void setup() {
        // inizializza manualmente tutti i mock necessari
        paymentMethodService = new PaymentMethodService(
                afmClient,
                paymentMethodRepository,
                paymentMethodFactory,
                npgClient,
                sessionUrlConfig,
                npgSessionsTemplateWrapper,
                npgDefaultApiKey,
                uniqueIdUtils,
                jwtSecretKey,
                900,
                jwtTokenUtils // <-- questo ora NON è null
        );
    }

    @Test
    void shouldCreatePaymentMethod() {
        Hooks.onOperatorDebug();

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

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
        PaymentMethodRequestDto paymentMethodRequestDto = new PaymentMethodRequestDto()
                .name(paymentMethod.getPaymentMethodName().value())
                .description(paymentMethod.getPaymentMethodName().value())
                .ranges(
                        paymentMethod.getPaymentMethodRanges().stream()
                                .map(p -> new RangeDto().max(p.max()).min(p.min())).toList()
                )
                .paymentTypeCode(paymentMethod.getPaymentMethodTypeCode().value())
                .asset(paymentMethod.getPaymentMethodAsset().value())
                .clientId(paymentMethod.getClientIdEnum())
                .methodManagement(paymentMethod.getPaymentMethodManagement().value())
                .brandAssets(paymentMethod.getPaymentMethodBrandAsset().brandAssets().orElse(null));
        PaymentMethod paymentMethodResponse = paymentMethodService.createPaymentMethod(
                paymentMethodRequestDto
        ).block();

        assertEquals(paymentMethodResponse.getPaymentMethodID(), paymentMethod.paymentMethodID());
    }

    @Test
    void shouldRetrievePaymentMethods() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdEnumCheckout = TestUtil.getClientIdCheckout();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        Mockito.when(paymentMethodRepository.findByClientId(clientIdEnumCheckout.getValue()))
                .thenReturn(Flux.just(paymentMethodDocument));

        PaymentMethod paymentMethodCreated = paymentMethodService
                .retrievePaymentMethods(null, clientIdEnumCheckout.getValue()).blockFirst();

        assertEquals(paymentMethodCreated.getPaymentMethodID(), paymentMethod.getPaymentMethodID());
    }

    @Test
    void shouldNotRetrievePaymentMethodsWithAmount() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        PaymentMethodRequestDto.ClientIdEnum clientIdEnumIo = TestUtil.getClientIdIO();

        Mockito.when(paymentMethodRepository.findByClientId(clientIdEnumIo.getValue()))
                .thenReturn(Flux.just(paymentMethodDocument));

        List<PaymentMethod> paymentMethodCreated = paymentMethodService
                .retrievePaymentMethods(101, clientIdEnumIo.getValue())
                .collectList().block();

        assertEquals(0, paymentMethodCreated.size());
    }

    @Test
    void shouldRetrievePaymentMethodsWithAmount() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        PaymentMethodRequestDto.ClientIdEnum clientIdEnumIo = TestUtil.getClientIdIO();

        Mockito.when(paymentMethodRepository.findByClientId(clientIdEnumIo.getValue()))
                .thenReturn(Flux.just(paymentMethodDocument));

        List<PaymentMethod> paymentmethodCreated = paymentMethodService
                .retrievePaymentMethods(50, clientIdEnumIo.getValue())
                .collectList().block();

        assertEquals(1, paymentmethodCreated.size());
    }

    @Test
    void shouldRetrieveSortedPaymentMethodsWithAmount() {
        Integer maxSize = new Random().nextInt(5, 10);
        PaymentMethodRequestDto.ClientIdEnum clientIdEnumCheckout = TestUtil.getClientIdCheckout();
        List<PaymentMethod> paymentMethodList = TestUtil.getAllPaymentMethod(maxSize, clientIdEnumCheckout, true);
        assertEquals(maxSize + 1, paymentMethodList.size());
        List<PaymentMethodDocument> paymentMethodDocumentList = paymentMethodList.stream()
                .map(pm -> TestUtil.getTestPaymentDoc(pm)).collect(Collectors.toList());

        Mockito.when(paymentMethodRepository.findByClientId(clientIdEnumCheckout.getValue()))
                .thenReturn(Flux.fromIterable(paymentMethodDocumentList));

        List<PaymentMethod> paymentMethodRetrieved = paymentMethodService
                .retrievePaymentMethods(50, clientIdEnumCheckout.getValue())
                .collectList().block();

        assertEquals(maxSize, paymentMethodRetrieved.size());
        assertEquals(TestUtil.CP_TYPE_CODE, paymentMethodRetrieved.get(0).getPaymentMethodTypeCode().value());
        assertEquals(TestUtil.TEST_DESC_FIRST, paymentMethodRetrieved.get(1).getPaymentMethodDescription().value());
        for (int i = 2; i < maxSize - 1; i++) {
            assertTrue(paymentMethodRetrieved.get(i).getPaymentMethodDescription().value().endsWith("_" + (i - 1)));
            String currentDescription = paymentMethodRetrieved.get(i).getPaymentMethodDescription().value();
            String previousDescription = paymentMethodRetrieved.get(i - 1).getPaymentMethodDescription().value();
            assertTrue(currentDescription.compareTo(previousDescription) >= 0);
        }

    }

    @Test
    void shouldRetrieveSortedPaymentMethodsWithoutAmount() {
        Integer maxSize = new Random().nextInt(5, 10);
        PaymentMethodRequestDto.ClientIdEnum clientIdEnumCheckout = TestUtil.getClientIdCheckout();
        List<PaymentMethod> paymentMethodList = TestUtil.getAllPaymentMethod(maxSize, clientIdEnumCheckout, false);
        assertEquals(maxSize, paymentMethodList.size());
        List<PaymentMethodDocument> paymentMethodDocumentList = paymentMethodList.stream()
                .map(pm -> TestUtil.getTestPaymentDoc(pm)).collect(Collectors.toList());

        Mockito.when(paymentMethodRepository.findByClientId(clientIdEnumCheckout.getValue()))
                .thenReturn(Flux.fromIterable(paymentMethodDocumentList));

        List<PaymentMethod> paymentMethodRetrieved = paymentMethodService
                .retrievePaymentMethods(null, clientIdEnumCheckout.getValue())
                .collectList().block();

        assertEquals(maxSize, paymentMethodRetrieved.size());
        assertEquals(TestUtil.CP_TYPE_CODE, paymentMethodRetrieved.get(0).getPaymentMethodTypeCode().value());
        assertEquals(TestUtil.TEST_DESC_FIRST, paymentMethodRetrieved.get(1).getPaymentMethodDescription().value());
        for (int i = 2; i < maxSize - 1; i++) {
            assertTrue(paymentMethodRetrieved.get(i).getPaymentMethodDescription().value().endsWith("_" + (i - 1)));
            String currentDescription = paymentMethodRetrieved.get(i).getPaymentMethodDescription().value();
            String previousDescription = paymentMethodRetrieved.get(i - 1).getPaymentMethodDescription().value();
            assertTrue(currentDescription.compareTo(previousDescription) >= 0);
        }

    }

    @Test
    void shouldPatchPaymentMethod() {

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
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
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdIO = TestUtil.getClientIdIO();

        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);

        Mockito.when(
                paymentMethodRepository.findByPaymentMethodIDAndClientId(
                        paymentMethod.getPaymentMethodID().value().toString(),
                        clientIdIO.getValue()
                )
        )
                .thenReturn(Mono.just(paymentMethodDocument));

        PaymentMethod paymentMethodCreated = paymentMethodService
                .retrievePaymentMethodById(paymentMethod.getPaymentMethodID().value().toString(), clientIdIO.getValue())
                .block();

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
                "CP",
                PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.getValue(),
                PaymentMethodManagementTypeDto.ONBOARDABLE.getValue(),
                null
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
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
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
                                        "CP",
                                        PaymentMethodRequestDto.ClientIdEnum.IO.getValue(),
                                        PaymentMethodManagementTypeDto.ONBOARDABLE.getValue(),
                                        null
                                )
                        )
                );

        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
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
                                        paymentTypeCode,
                                        PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.getValue(),
                                        PaymentMethodManagementTypeDto.ONBOARDABLE.getValue(),
                                        null
                                )
                        )
                );

        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(paymentTypeCode, serviceResponse.getBundles().get(0).getPaymentMethod());
    }

    @Test
    void shouldCreateSessionWithJwtException() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();
        String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 15);

        Mockito.when(uniqueIdUtils.generateUniqueId()).thenReturn(Mono.just(orderId));
        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(jwtTokenUtils.generateToken(any(), anyInt(), any(Claims.class)))
                .thenReturn(Either.left(new JWTTokenGenerationException()));

        StepVerifier.create(paymentMethodService.createSessionForPaymentMethod(paymentMethodId, null))
                .expectError(JWTTokenGenerationException.class)
                .verify();
    }

    @Test
    void shouldCreateSessionForValidPaymentMethod() {
        when(jwtTokenUtils.generateToken(any(), anyInt(), any())).thenReturn(Either.right("token"));

        UUID correlationId = UUID.randomUUID();
        try (MockedStatic<UUID> uuidStaticMock = Mockito.mockStatic(UUID.class)) {
            uuidStaticMock.when(UUID::randomUUID).thenReturn(correlationId);
            PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
            PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
            String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();
            FieldsDto npgResponse = TestUtil.npgResponse();
            String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 15);
            Mockito.when(uniqueIdUtils.generateUniqueId()).thenReturn(Mono.just(orderId));
            Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                    .thenReturn(Mono.just(paymentMethodDocument));
            Mockito.when(jwtTokenUtils.generateToken(any(), anyInt(), any(Claims.class)))
                    .thenReturn(Either.right("sessionToken"));
            Mockito.when(
                    npgClient.buildForm(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            )
                    .thenReturn(
                            Mono.just(npgResponse)
                    );
            Mockito.doNothing().when(npgSessionsTemplateWrapper).save(any());

            CreateSessionResponseDto expected = new CreateSessionResponseDto()
                    .orderId(orderId)
                    .correlationId(correlationId)
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

            StepVerifier.create(paymentMethodService.createSessionForPaymentMethod(paymentMethodId, null))
                    .expectNext(expected)
                    .verifyComplete();

            // Check url contain random t queryparams
            ArgumentCaptor<URI> resultUrlCaptor = ArgumentCaptor.forClass(URI.class);
            ArgumentCaptor<URI> cancelUrl = ArgumentCaptor.forClass(URI.class);
            Mockito.verify(npgClient, times(1)).buildForm(
                    any(),
                    any(),
                    resultUrlCaptor.capture(),
                    any(),
                    cancelUrl.capture(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
            );
            assertTrue(
                    () -> TestUtil.urlContainsRandomTQueryParam(resultUrlCaptor.getValue())
                            && TestUtil.urlContainsRandomTQueryParam(cancelUrl.getValue())
            );
        }
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
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.empty());
        StepVerifier.create(paymentMethodService.getCardDataInformation(paymentMethodId, any()))
                .expectErrorMatches(e -> e instanceof OrderIdNotFoundException)
                .verify();

        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).findById(any());
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(0)).save(any());
        Mockito.verify(npgClient, Mockito.times(0)).getCardData(any(), any(), any());
    }

    @Test
    void shouldRetrieveCardDataWithCacheMiss() {
        String paymentMethodId = "paymentMethodId";
        String orderId = "orderId";
        String sessionId = "sessionId";
        String correlationId = UUID.randomUUID().toString();
        CardDataResponseDto npgResponse = TestUtil.npgCardDataResponse();
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        SessionPaymentMethodResponseDto expectedResponse = new SessionPaymentMethodResponseDto()
                .bin(npgResponse.getBin())
                .sessionId(sessionId)
                .expiringDate(npgResponse.getExpiringDate())
                .lastFourDigits(npgResponse.getLastFourDigits())
                .brand(npgResponse.getCircuit());
        NpgSessionDocument npgSessionDocument = TestUtil
                .npgSessionDocument(orderId, correlationId, sessionId, false, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(orderId)).thenReturn(Optional.of(npgSessionDocument));
        Mockito.when(npgClient.getCardData(any(), any(), any())).thenReturn(Mono.just(npgResponse));
        /* Tests */
        StepVerifier.create(paymentMethodService.getCardDataInformation(paymentMethodId, orderId))
                .expectNext(expectedResponse)
                .verifyComplete();
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).findById(any());
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).save(any());
        Mockito.verify(npgClient, Mockito.times(1))
                .getCardData(eq(UUID.fromString(correlationId)), eq(sessionId), any());
    }

    @Test
    void shouldRetrieveCardDataWithCacheHit() {
        String paymentMethodId = "paymentMethodId";
        String orderId = "orderId";
        String sessionId = "sessionId";
        String correlationId = UUID.randomUUID().toString();
        CardDataResponseDto npgResponse = TestUtil.npgCardDataResponse();
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        SessionPaymentMethodResponseDto expectedResponse = new SessionPaymentMethodResponseDto()
                .bin(npgResponse.getBin()).sessionId(sessionId).expiringDate(npgResponse.getExpiringDate())
                .lastFourDigits(npgResponse.getLastFourDigits())
                .brand(npgResponse.getCircuit());
        NpgSessionDocument npgSessionDocument = TestUtil
                .npgSessionDocument(orderId, correlationId, sessionId, true, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(orderId)).thenReturn(Optional.of(npgSessionDocument));

        /* Tests */
        StepVerifier.create(paymentMethodService.getCardDataInformation(paymentMethodId, orderId))
                .expectNext(expectedResponse)
                .verifyComplete();
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(1)).findById(any());
        Mockito.verify(npgSessionsTemplateWrapper, Mockito.times(0)).save(any());
        Mockito.verify(npgClient, Mockito.times(0)).getCardData(any(), any(), any());
    }

    @Test
    void shouldReturnTransactionIdForValidSession() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();
        String correlationId = UUID.randomUUID().toString();
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        NpgSessionDocument npgSessionDocument = TestUtil
                .npgSessionDocument("orderId", correlationId, "sessionId", false, transactionId.value());

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(TestUtil.getTestPaymentDoc(paymentMethod)));
        Mockito.when(npgSessionsTemplateWrapper.findById(any())).thenReturn(Optional.of(npgSessionDocument));

        StepVerifier
                .create(
                        paymentMethodService.isSessionValid(
                                paymentMethodId,
                                npgSessionDocument.orderId(),
                                npgSessionDocument.securityToken()
                        )
                )
                .expectNext(transactionId)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorForInvalidSession() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();
        String correlationId = UUID.randomUUID().toString();
        NpgSessionDocument npgSessionDocument = TestUtil
                .npgSessionDocument("orderId", correlationId, "sessionId", false, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(TestUtil.getTestPaymentDoc(paymentMethod)));
        Mockito.when(npgSessionsTemplateWrapper.findById(any())).thenReturn(Optional.of(npgSessionDocument));

        StepVerifier
                .create(
                        paymentMethodService
                                .isSessionValid(paymentMethodId, npgSessionDocument.orderId(), "OTHER_SECURITY_TOKEN")
                )
                .expectError(InvalidSessionException.class)
                .verify();
    }

    @Test
    void shouldReturnErrorForSessionNotFound() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        String paymentMethodId = paymentMethod.getPaymentMethodID().value().toString();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(TestUtil.getTestPaymentDoc(paymentMethod)));
        Mockito.when(npgSessionsTemplateWrapper.findById(any())).thenReturn(Optional.empty());

        StepVerifier
                .create(
                        paymentMethodService
                                .isSessionValid(paymentMethodId, "NON_EXISTING_ORDER_ID", "SECURITY_TOKEN")
                )
                .expectError(OrderIdNotFoundException.class)
                .verify();
    }

    @Test
    void shouldReturnErrorForNonExistingMethod() {
        Mockito.when(paymentMethodRepository.findById(anyString())).thenReturn(Mono.empty());

        StepVerifier
                .create(
                        paymentMethodService
                                .isSessionValid("NON_EXISTING_METHOD_ID", "NON_EXISTING_ORDER_ID", "SECURITY_TOKEN")
                )
                .expectError(PaymentMethodNotFoundException.class)
                .verify();
    }

    @Test
    void shouldUpdateSessionData() {
        String sessionId = "sessionId";
        String orderId = "orderId";
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";
        String correlationId = UUID.randomUUID().toString();
        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);
        NpgSessionDocument npgSessionDocument = TestUtil
                .npgSessionDocument(orderId, correlationId, sessionId, true, null);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(orderId)).thenReturn(Optional.of(npgSessionDocument));

        NpgSessionDocument expectedResponse = new NpgSessionDocument(
                npgSessionDocument.orderId(),
                npgSessionDocument.correlationId(),
                npgSessionDocument.sessionId(),
                npgSessionDocument.securityToken(),
                npgSessionDocument.cardData(),
                patchSessionRequestDto.getTransactionId()
        );

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, orderId, patchSessionRequestDto))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void shouldReturnOkOnSessionAlreadyAssociatedToTransactionId() {
        String sessionId = "sessionId";
        String orderId = "orderId";
        String correlationId = UUID.randomUUID().toString();
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);
        NpgSessionDocument npgSessionDocument = TestUtil
                .npgSessionDocument(orderId, correlationId, sessionId, true, transactionId);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(orderId)).thenReturn(Optional.of(npgSessionDocument));

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, orderId, patchSessionRequestDto))
                .expectNext(npgSessionDocument)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorOnSessionAlreadyAssociatedToDifferentTransactionId() {
        String sessionId = "sessionId";
        String orderId = "orderId";
        String correlationId = UUID.randomUUID().toString();
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);
        NpgSessionDocument npgSessionDocument = TestUtil
                .npgSessionDocument(orderId, correlationId, sessionId, true, "ANOTHER_TRANSACTION_ID");

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(orderId)).thenReturn(Optional.of(npgSessionDocument));

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, orderId, patchSessionRequestDto))
                .expectError(SessionAlreadyAssociatedToTransaction.class)
                .verify();
    }

    @Test
    void shouldReturnErrorOnNonExistingSession() {
        String sessionId = "sessionId";
        String orderId = "orderId";
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(npgSessionsTemplateWrapper.findById(sessionId)).thenReturn(Optional.empty());

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, orderId, patchSessionRequestDto))
                .expectError(OrderIdNotFoundException.class)
                .verify();
    }

    @Test
    void shouldReturnErrorOnNonExistingPaymentMethod() {
        String orderId = "orderId";
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        String paymentMethodId = paymentMethodDocument.getPaymentMethodID();
        String transactionId = "transactionId";

        PatchSessionRequestDto patchSessionRequestDto = new PatchSessionRequestDto().transactionId(transactionId);

        Mockito.when(paymentMethodRepository.findById(paymentMethodId)).thenReturn(Mono.empty());

        StepVerifier.create(paymentMethodService.updateSession(paymentMethodId, orderId, patchSessionRequestDto))
                .expectError(PaymentMethodNotFoundException.class)
                .verify();
    }

    public static Stream<Arguments> gecInvalidTransferDtoSource() {
        return Stream.of(Arguments.of(List.<TransferDto>of()), Arguments.of((List<TransferDto>) null));
    }

    @ParameterizedTest
    @MethodSource("gecInvalidTransferDtoSource")
    void shouldReturnNoBundleFoundExceptionForNoBundleReturnedByGec(List<TransferDto> invalidTransferDto) {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.getCalculateFeeRequest();
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();
        gecResponse.setBundleOptions(invalidTransferDto);
        PaymentMethodDocument paymentMethodDocument = new PaymentMethodDocument(
                UUID.randomUUID().toString(),
                NpgClient.PaymentMethod.CARDS.serviceName,
                "Description",
                PaymentMethodStatusEnum.ENABLED.getCode(),
                "asset",
                List.of(Pair.of(0L, 100L)),
                "CP",
                PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.getValue(),
                PaymentMethodManagementTypeDto.ONBOARDABLE.getValue(),
                null
        );
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(
                        Mono.just(
                                paymentMethodDocument
                        )
                );
        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        StepVerifier.create(
                paymentMethodService
                        .computeFee(calculateFeeRequestDto, paymentMethodId, null)
        )
                .expectError(NoBundleFoundException.class)
                .verify();
    }

    @Test
    void shouldReturnsSortedFeeListWithoutOnUs() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.getCalculateFeeRequest();
        final var gecResponse = TestUtil.getBundleOptionDtoClientResponseWithUnsortedTransferListAllNotOnUs();
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(paymentMethodDocument.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                paymentMethodDocument.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
        for (int i = 0; i < gecResponse.getBundleOptions().size() - 2; i++) {
            assertTrue(
                    serviceResponse.getBundles().get(i).getTaxPayerFee().intValue() < serviceResponse.getBundles()
                            .get(i + 1).getTaxPayerFee().intValue()
            );
        }
        assertTrue(
                serviceResponse.getBundles().stream().max(
                        (
                         b1,
                         b2
                        ) -> (int) (b1.getTaxPayerFee() - b2.getTaxPayerFee())
                ).get().getTaxPayerFee().equals(
                        serviceResponse.getBundles().get(gecResponse.getBundleOptions().size() - 1).getTaxPayerFee()
                )
        );
        assertTrue(
                serviceResponse.getBundles().stream().min(
                        (
                         b1,
                         b2
                        ) -> (int) (b1.getTaxPayerFee() - b2.getTaxPayerFee())
                ).get().getTaxPayerFee().equals(serviceResponse.getBundles().get(0).getTaxPayerFee())
        );
    }

    @Test
    void shouldReturnsSortedFeeListPlacingUniqueOnUsInFirstPosition() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.getCalculateFeeRequest();
        final var gecResponse = TestUtil.getBundleOptionDtoClientResponseWithUnsortedTransferListOnlyOneOnUs();
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(paymentMethodDocument.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                paymentMethodDocument.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
        assertTrue(serviceResponse.getBundles().get(0).getOnUs());
        assertFalse(
                serviceResponse.getBundles().get(0).getTaxPayerFee().intValue() < serviceResponse.getBundles().get(1)
                        .getTaxPayerFee().intValue()
        );
        for (int i = 1; i < gecResponse.getBundleOptions().size() - 2; i++) {
            assertTrue(
                    serviceResponse.getBundles().get(i).getTaxPayerFee().intValue() < serviceResponse.getBundles()
                            .get(i + 1).getTaxPayerFee().intValue()
            );
        }
    }

    @Test
    void shouldReturnsFeeListMaintainingSortingForOnUsAndNotOnUsBundles() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.getCalculateFeeRequest();
        final var gecResponse = TestUtil.getBundleOptionDtoClientResponseWithUnsortedTransferMixedWithSameFees();
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(paymentMethodDocument));
        Mockito.when(afmClient.getFees(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(paymentMethodDocument.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                paymentMethodDocument.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
        assertTrue(serviceResponse.getBundles().get(0).getOnUs());
        assertFalse(serviceResponse.getBundles().get(1).getOnUs());
        assertFalse(serviceResponse.getBundles().get(2).getOnUs());
        assertFalse(serviceResponse.getBundles().get(3).getOnUs());
        assertFalse(serviceResponse.getBundles().get(4).getOnUs());
        assertFalse(serviceResponse.getBundles().get(5).getOnUs());

        List<it.pagopa.ecommerce.payment.methods.server.model.BundleDto> serviceIdPspOnUsList = serviceResponse
                .getBundles().stream().filter(it.pagopa.ecommerce.payment.methods.server.model.BundleDto::getOnUs)
                .toList();
        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> gecIdPspOnUsList = gecResponse.getBundleOptions()
                .stream().filter(it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto::getOnUs)
                .toList();

        List<it.pagopa.ecommerce.payment.methods.server.model.BundleDto> serviceIdPspNotOnUsList = serviceResponse
                .getBundles().stream()
                .filter(bundleDto -> !bundleDto.getOnUs()).toList();
        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> gecIdPspNotOnUsList = gecResponse.getBundleOptions()
                .stream()
                .filter(transferDto -> Boolean.FALSE.equals(transferDto.getOnUs())).toList();

        for (int i = 0; i < serviceIdPspOnUsList.size(); i++) {
            assertTrue(serviceIdPspOnUsList.get(i).getIdPsp().equals(gecIdPspOnUsList.get(i).getIdPsp()));
        }

        boolean samePositionForAllelements = true;
        for (int i = 0; i < serviceIdPspNotOnUsList.size(); i++) {
            samePositionForAllelements &= serviceIdPspNotOnUsList.get(i).getIdPsp()
                    .equals(gecIdPspNotOnUsList.get(i).getIdPsp());
        }
        assertFalse(samePositionForAllelements);
    }

}
