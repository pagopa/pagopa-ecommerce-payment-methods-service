package it.pagopa.ecommerce.payment.methods.service.v2;

import it.pagopa.ecommerce.payment.methods.application.v2.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.client.PaymentMethodsHandlerClient;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeResponseDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto;
import it.pagopa.generated.ecommerce.handler.v1.dto.PaymentMethodResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class PaymentMethodServiceTests {

    private final AfmClient afmClient = mock(AfmClient.class);

    private final NpgSessionsTemplateWrapper npgSessionsTemplateWrapper = mock(NpgSessionsTemplateWrapper.class);
    private final PaymentMethodsHandlerClient paymentMethodsHandlerClient = mock(PaymentMethodsHandlerClient.class);

    private final PaymentMethodService paymentMethodService = new PaymentMethodService(
            afmClient,
            npgSessionsTemplateWrapper,
            paymentMethodsHandlerClient
    );

    private final PaymentMethodResponseDto paymentMethodResponseDto = new PaymentMethodResponseDto()
            .paymentTypeCode("CP")
            .name(Map.of("it", "CARDS"))
            .description(Map.of("it", "Description"))
            .status(PaymentMethodResponseDto.StatusEnum.ENABLED)
            .paymentMethodAsset("asset")
            .paymentMethodsBrandAssets(null);

    @Test
    void shouldRetrieveFeeForMultiplePaymentNotice() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();
        Mockito.when(paymentMethodsHandlerClient.validatePaymentMethodExists(paymentMethodId, null))
                .thenReturn(Mono.just(paymentMethodResponseDto));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals("CARDS", serviceResponse.getPaymentMethodName());
        assertEquals(
                "Description",
                serviceResponse.getPaymentMethodDescription()
        );
    }

    @Test
    void shouldRetrieveFeeForMultiplePaymentNoticeWithoutPspList() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        calculateFeeRequestDto.setIdPspList(null);
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();

        Mockito.when(paymentMethodsHandlerClient.validatePaymentMethodExists(paymentMethodId, null))
                .thenReturn(Mono.just(paymentMethodResponseDto));

        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
    }

    @Test
    void shouldRetrieveFeeForMultiplePaymentNoticeWithPspWithNullPaymentType() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        calculateFeeRequestDto.setIdPspList(null);
        final var gecResponse = TestUtil.V2.getBundleOptionWithAnyValueDtoClientResponse();
        String paymentTypeCode = "CP";

        Mockito.when(paymentMethodsHandlerClient.validatePaymentMethodExists(paymentMethodId, null))
                .thenReturn(Mono.just(paymentMethodResponseDto));

        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
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
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();
        gecResponse.setBundleOptions(invalidTransferDto);
        Mockito.when(paymentMethodsHandlerClient.validatePaymentMethodExists(paymentMethodId, null))
                .thenReturn(Mono.just(paymentMethodResponseDto));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        StepVerifier.create(
                paymentMethodService
                        .computeFee(calculateFeeRequestDto, paymentMethodId, null)
        )
                .expectError(NoBundleFoundException.class)
                .verify();
    }

}
