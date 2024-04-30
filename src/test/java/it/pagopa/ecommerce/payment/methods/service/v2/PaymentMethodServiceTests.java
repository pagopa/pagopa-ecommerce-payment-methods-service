package it.pagopa.ecommerce.payment.methods.service.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.payment.methods.application.v2.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeRequestDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeResponseDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.PaymentMethodManagementTypeDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PaymentMethodServiceTests {

    private final AfmClient afmClient = mock(AfmClient.class);

    private final PaymentMethodRepository paymentMethodRepository = mock(PaymentMethodRepository.class);

    private final PaymentMethodService paymentMethodService = new PaymentMethodService(
            paymentMethodRepository,
            afmClient
    );

    @Test
    void shouldRetrieveFeeForMultiplePaymentNotice() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesMulti(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(PAYMENT_METHOD_TEST.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                PAYMENT_METHOD_TEST.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
    }

    @Test
    void shouldRetrieveFeeForMultiplePaymentNoticeWithoutPspList() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        calculateFeeRequestDto.setIdPspList(null);
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));

        Mockito.when(afmClient.getFeesMulti(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
    }

    @Test
    void shouldRetrieveFeeForMultiplePaymentNoticeWithPspWithNullPaymentType() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        calculateFeeRequestDto.setIdPspList(null);
        BundleOptionDto gecResponse = TestUtil.getBundleOptionWithAnyValueDtoClientResponse();
        String paymentTypeCode = "CP";

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));

        Mockito.when(afmClient.getFeesMulti(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(paymentTypeCode, serviceResponse.getBundles().get(0).getPaymentMethod());
    }

    public static Stream<Arguments> gecInvalidTransferDtoSource() {
        return Stream.of(Arguments.of(List.<TransferDto>of()), Arguments.of((List<TransferDto>) null));
    }

    @ParameterizedTest
    @MethodSource("gecInvalidTransferDtoSource")
    void shouldReturnNoBundleFoundExceptionForNoBundleReturnedByGec(List<TransferDto> invalidTransferDto) {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();
        gecResponse.setBundleOptions(invalidTransferDto);
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesMulti(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        StepVerifier.create(
                paymentMethodService
                        .computeFee(calculateFeeRequestDto, paymentMethodId, null)
        )
                .expectError(NoBundleFoundException.class)
                .verify();
    }

    private final PaymentMethodDocument PAYMENT_METHOD_TEST = new PaymentMethodDocument(
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
}
