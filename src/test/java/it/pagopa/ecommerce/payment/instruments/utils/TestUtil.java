package it.pagopa.ecommerce.payment.instruments.utils;

import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentMethodResponseDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PspDto;
import it.pagopa.ecommerce.payment.instruments.server.model.RangeDto;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.PageInfoDto;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServiceDto;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestUtil {

    static final String TEST_NAME = "Test";
    static final  String TEST_DESC = "test";
    static final  PaymentMethodRequestDto.StatusEnum TEST_STATUS = PaymentMethodRequestDto.StatusEnum.ENABLED;
    static final String TEST_TYPE_CODE = "test";

    static final String TEST_LANG = "IT";
    static final Long TEST_AMOUNT = 1L;

    static final UUID TEST_ID = UUID.randomUUID();
    static final String PSP_TEST_CODE = "001";
    static final String PSP_TEST_NAME = "test";
    static final String PSP_TEST_DESC = "test";
    static final String PSP_TEST_CHANNEL = "channel0";

    public static PaymentMethod getPaymentMethod() {
        return new PaymentMethod(
                new PaymentMethodID(TEST_ID),
                new PaymentMethodName("Test"),
                new PaymentMethodDescription("Test"),
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                new PaymentMethodType(TEST_TYPE_CODE),
                List.of(new PaymentMethodRange(0L, 100L))
        );
    }

    public static PaymentMethodRequestDto getPaymentMethodRequest(){
        return new PaymentMethodRequestDto()
                .name(TEST_NAME)
                .description(TEST_DESC)
                .status(TEST_STATUS)
                .paymentTypeCode(TEST_TYPE_CODE)
                .ranges(List.of(new RangeDto().max(100L).min(0L)));
    }

    public static PaymentMethodResponseDto getPaymentMethodResponse(PaymentMethod paymentMethod){
        return new PaymentMethodResponseDto()
                .id(paymentMethod.getPaymentMethodID().value().toString())
                .name(paymentMethod.getPaymentMethodName().value())
                .description(paymentMethod.getPaymentMethodDescription().value())
                .status(PaymentMethodResponseDto.StatusEnum.fromValue(paymentMethod.getPaymentMethodStatus().value().getCode()))
                .paymentTypeCode(paymentMethod.getPaymentMethodTypeCode().value())
                .ranges(List.of(new RangeDto().min(0L).max(100L)));
    }

    public static long getTestAmount(){
        return TEST_AMOUNT;
    }

    public static PspDto getTestPsp(){
        return new PspDto()
                .code(PSP_TEST_CODE)
                .brokerName(PSP_TEST_NAME)
                .description(PSP_TEST_DESC)
                .businessName(PSP_TEST_NAME)
                .status(PspDto.StatusEnum.ENABLED)
                .channelCode(PSP_TEST_CHANNEL);
    }

    public static String getTestLang(){
        return TEST_LANG;
    }

    public static String getTestPaymentType(){
        return TEST_TYPE_CODE;
    }

    public static ServicesDto getTestServices(){
        return new ServicesDto()
                .services(List.of(new ServiceDto()
                        .abiCode("TEST")
                        .channelCode("CHANNEL=")
                        .languageCode(ServiceDto.LanguageCodeEnum.IT)
                        .conventionCode("TEST")
                        .pspCode("TEST")
                        .serviceName("TEST")))
                .pageInfo(
                        new PageInfoDto()
                                .totalPages(1)
                                .limit(50)
                                .page(0)
                                .itemsFound(1)
                );
    }

    public static PaymentMethodDocument getTestPaymentDoc(PaymentMethod paymentMethod){
        return new PaymentMethodDocument(
                paymentMethod.getPaymentMethodID().value().toString(),
                paymentMethod.getPaymentMethodName().value(),
                paymentMethod.getPaymentMethodDescription().value(),
                paymentMethod.getPaymentMethodStatus().value().toString(),
                paymentMethod.getPaymentMethodRanges().stream().map(r -> Pair.of(r.min(), r.max()))
                        .collect(Collectors.toList()),
                paymentMethod.getPaymentMethodTypeCode().value());
    }
}