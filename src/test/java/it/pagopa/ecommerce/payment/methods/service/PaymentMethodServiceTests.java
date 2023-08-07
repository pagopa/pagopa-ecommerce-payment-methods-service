package it.pagopa.ecommerce.payment.methods.service;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeResponseDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTests {

    @Mock
    private AfmClient afmClient;

    @Mock
    private NpgClient npgClient;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentMethodFactory paymentMethodFactory;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    @Test
    void shouldCreatePaymentMethod() {
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
                "Carte",
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
        Mockito.when(afmClient.getFees(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        paymentMethodService = new PaymentMethodService(
                afmClient,
                paymentMethodRepository,
                paymentMethodFactory,
                npgClient
        );

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
                                        "Carte",
                                        "",
                                        PaymentMethodStatusEnum.ENABLED.getCode(),
                                        "asset",
                                        List.of(Pair.of(0L, 100L)),
                                        "CP"
                                )
                        )
                );

        Mockito.when(afmClient.getFees(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        paymentMethodService = new PaymentMethodService(
                afmClient,
                paymentMethodRepository,
                paymentMethodFactory,
                npgClient
        );

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
                                        "Carte",
                                        "",
                                        PaymentMethodStatusEnum.ENABLED.getCode(),
                                        "asset",
                                        List.of(Pair.of(0L, 100L)),
                                        paymentTypeCode
                                )
                        )
                );

        Mockito.when(afmClient.getFees(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        paymentMethodService = new PaymentMethodService(
                afmClient,
                paymentMethodRepository,
                paymentMethodFactory,
                npgClient
        );

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(Mono.just(calculateFeeRequestDto), paymentMethodId, null).block();
        assertEquals(paymentTypeCode, serviceResponse.getBundles().get(0).getPaymentMethod());
    }

}
