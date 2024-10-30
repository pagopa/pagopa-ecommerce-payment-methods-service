package it.pagopa.ecommerce.payment.methods.service.v2;

import static com.mongodb.assertions.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import it.pagopa.ecommerce.payment.methods.v2.server.model.BundleDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeResponseDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.PaymentMethodManagementTypeDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto;
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
    void shouldReturnsSortedFeeListWithoutOnUs() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponseWithUnsortedTransferListAllNotOnUs();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(PAYMENT_METHOD_TEST.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                PAYMENT_METHOD_TEST.getPaymentMethodDescription(),
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
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponseWithUnsortedTransferListOnlyOneOnUs();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(PAYMENT_METHOD_TEST.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                PAYMENT_METHOD_TEST.getPaymentMethodDescription(),
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
    void shouldReturnsSortedFeeListPlacingOnUsInFirstAndSecondPositionSortedByFees() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponseWithUnsortedTransferListTwoOnUs();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(PAYMENT_METHOD_TEST.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                PAYMENT_METHOD_TEST.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
        assertTrue(serviceResponse.getBundles().get(0).getOnUs());
        assertTrue(serviceResponse.getBundles().get(1).getOnUs());
        assertFalse(serviceResponse.getBundles().get(2).getOnUs());
        assertFalse(serviceResponse.getBundles().get(3).getOnUs());

        assertTrue(
                serviceResponse.getBundles().get(0).getTaxPayerFee().intValue() < serviceResponse.getBundles().get(1)
                        .getTaxPayerFee().intValue()
        );
        assertTrue(
                serviceResponse.getBundles().get(2).getTaxPayerFee().intValue() < serviceResponse.getBundles().get(3)
                        .getTaxPayerFee().intValue()
        );
        assertFalse(
                serviceResponse.getBundles().get(1).getTaxPayerFee().intValue() < serviceResponse.getBundles().get(2)
                        .getTaxPayerFee().intValue()
        );
    }

    @Test
    void shouldReturnsSortedFeeListForAllOnUSBundles() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponseWithUnsortedTransferListAllOnUs();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(PAYMENT_METHOD_TEST.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                PAYMENT_METHOD_TEST.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
        for (int i = 0; i < gecResponse.getBundleOptions().size() - 2; i++) {
            assertTrue(
                    serviceResponse.getBundles().get(i).getTaxPayerFee().intValue() <= serviceResponse.getBundles()
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
    void shouldReturnsFeeListMaintainingSortingForOnUsAndNotOnUsBundles() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponseWithUnsortedTransferMixedWithSameFees();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(PAYMENT_METHOD_TEST.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                PAYMENT_METHOD_TEST.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
        assertTrue(serviceResponse.getBundles().get(0).getOnUs());
        assertTrue(serviceResponse.getBundles().get(1).getOnUs());
        assertTrue(serviceResponse.getBundles().get(2).getOnUs());
        assertFalse(serviceResponse.getBundles().get(3).getOnUs());
        assertFalse(serviceResponse.getBundles().get(4).getOnUs());
        assertFalse(serviceResponse.getBundles().get(5).getOnUs());

        List<BundleDto> serviceIdPspOnUsList = serviceResponse.getBundles().stream().filter(BundleDto::getOnUs)
                .toList();
        List<TransferDto> gecIdPspOnUsList = gecResponse.getBundleOptions().stream().filter(TransferDto::getOnUs)
                .toList();

        List<BundleDto> serviceIdPspNotOnUsList = serviceResponse.getBundles().stream()
                .filter(bundleDto -> !bundleDto.getOnUs()).toList();
        List<TransferDto> gecIdPspNotOnUsList = gecResponse.getBundleOptions().stream()
                .filter(transferDto -> Boolean.FALSE.equals(transferDto.getOnUs())).toList();

        for (int i = 0; i < serviceIdPspOnUsList.size(); i++) {
            assertTrue(serviceIdPspOnUsList.get(i).getIdPsp().equals(gecIdPspOnUsList.get(i).getIdPsp()));
        }

        for (int i = 0; i < serviceIdPspNotOnUsList.size(); i++) {
            assertTrue(serviceIdPspNotOnUsList.get(i).getIdPsp().equals(gecIdPspNotOnUsList.get(i).getIdPsp()));
        }

    }

    @Test
    void shouldRetrieveFeeForMultiplePaymentNotice() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
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
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        calculateFeeRequestDto.setIdPspList(null);
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));

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

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));

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
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
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
