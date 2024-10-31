package it.pagopa.ecommerce.payment.methods.utils;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.CardDataResponseDto;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldDto;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.WorkflowStateDto;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.methods.infrastructure.CardDataDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.v2.server.model.PaymentNoticeDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.PspSearchCriteriaDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.PaymentNoticeItemDto;
import java.util.stream.LongStream;
import org.springframework.data.util.Pair;

import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class TestUtil {

    static final UUID CP_UUID_ID = UUID.randomUUID();
    static final String TEST_NAME = NpgClient.PaymentMethod.CARDS.serviceName;
    static final String TEST_DESC = "test";
    public static final String TEST_DESC_FIRST = "AAA_FIRST_DESCRIPTION";
    static final PaymentMethodStatusDto TEST_STATUS = PaymentMethodStatusDto.ENABLED;
    static final String TEST_TYPE_CODE = "test";
    public static final String CP_TYPE_CODE = "CP";

    static final String TEST_LANG = "IT";
    static final Long TEST_AMOUNT = 1L;

    static final String TEST_ASSET = "test";

    static final UUID TEST_ID = UUID.randomUUID();
    static final String PSP_TEST_CODE = "001";
    static final String PSP_TEST_NAME = "test";
    static final String PSP_TEST_DESC = "test";
    static final String PSP_TEST_CHANNEL = "channel0";

    static final NpgClient.PaymentMethod TEST_NPG_PAYMENT_METHOD = NpgClient.PaymentMethod.CARDS;

    public static PaymentMethod getNPGPaymentMethod() {
        return new PaymentMethod(
                new PaymentMethodID(TEST_ID),
                new PaymentMethodName(TEST_NAME),
                new PaymentMethodDescription(TEST_DESC),
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                new PaymentMethodType(TEST_TYPE_CODE),
                List.of(new PaymentMethodRange(0L, 100L)),
                new PaymentMethodAsset(TEST_ASSET),
                getClientIdCheckout(),
                new PaymentMethodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE),
                new PaymentMethodBrandAssets(Optional.empty())
        );
    }

    public static PaymentMethod getRedirectPaymentMethod() {
        return new PaymentMethod(
                new PaymentMethodID(TEST_ID),
                new PaymentMethodName("TEST_NAME"),
                new PaymentMethodDescription(TEST_DESC),
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                new PaymentMethodType("RBPR"),
                List.of(new PaymentMethodRange(0L, 100L)),
                new PaymentMethodAsset(TEST_ASSET),
                getClientIdCheckout(),
                new PaymentMethodManagement(PaymentMethodManagementTypeDto.REDIRECT),
                new PaymentMethodBrandAssets(Optional.empty())
        );
    }

    public static List<PaymentMethod> getAllPaymentMethod(
                                                          int maxIndex,
                                                          PaymentMethodRequestDto.ClientIdEnum clientIdEnum,
                                                          boolean addOutOfRange
    ) {
        List<PaymentMethod> toSort = new ArrayList();
        for (int i = maxIndex - 2; i > 0; i--) {
            toSort.add(
                    new PaymentMethod(
                            new PaymentMethodID(UUID.randomUUID()),
                            new PaymentMethodName(TEST_NAME),
                            new PaymentMethodDescription(TEST_DESC + "_" + i),
                            new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                            new PaymentMethodType(TEST_TYPE_CODE + "_" + i),
                            List.of(new PaymentMethodRange(0L, 100L)),
                            new PaymentMethodAsset(TEST_ASSET + "_" + i),
                            clientIdEnum,
                            new PaymentMethodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE),
                            new PaymentMethodBrandAssets(Optional.empty())
                    )
            );
        }
        toSort.add(
                new PaymentMethod(
                        new PaymentMethodID(UUID.randomUUID()),
                        new PaymentMethodName(TEST_NAME),
                        new PaymentMethodDescription(TEST_DESC_FIRST),
                        new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                        new PaymentMethodType(TEST_TYPE_CODE),
                        List.of(new PaymentMethodRange(0L, 100L)),
                        new PaymentMethodAsset(TEST_ASSET),
                        clientIdEnum,
                        new PaymentMethodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE),
                        new PaymentMethodBrandAssets(Optional.empty())
                )
        );
        if (addOutOfRange) {
            toSort.add(
                    new PaymentMethod(
                            new PaymentMethodID(UUID.randomUUID()),
                            new PaymentMethodName(TEST_NAME),
                            new PaymentMethodDescription(TEST_DESC),
                            new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                            new PaymentMethodType(TEST_TYPE_CODE),
                            List.of(new PaymentMethodRange(Long.MAX_VALUE - 1, Long.MAX_VALUE)),
                            new PaymentMethodAsset(TEST_ASSET),
                            clientIdEnum,
                            new PaymentMethodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE),
                            new PaymentMethodBrandAssets(Optional.empty())
                    )
            );
        }
        toSort.add(
                toSort.size() - 2,
                getCPPaymentMethod(clientIdEnum)
        );
        return toSort;
    }

    public static PaymentMethod getCPPaymentMethod(PaymentMethodRequestDto.ClientIdEnum clientIdEnum) {
        return new PaymentMethod(
                new PaymentMethodID(CP_UUID_ID),
                new PaymentMethodName(TEST_NAME),
                new PaymentMethodDescription(TEST_DESC),
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                new PaymentMethodType(CP_TYPE_CODE),
                List.of(new PaymentMethodRange(0L, 100L)),
                new PaymentMethodAsset(TEST_ASSET),
                clientIdEnum,
                new PaymentMethodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE),
                new PaymentMethodBrandAssets(Optional.empty())
        );
    }

    public static PaymentMethodRequestDto getPaymentMethodRequestForCheckout() {
        return new PaymentMethodRequestDto()
                .clientId(getClientIdCheckout())
                .name(TEST_NAME)
                .description(TEST_DESC)
                .status(TEST_STATUS)
                .paymentTypeCode(TEST_TYPE_CODE)
                .ranges(List.of(new RangeDto().max(100L).min(0L)))
                .asset(TEST_ASSET)
                .methodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE);
    }

    public static PaymentMethodRequestDto getPaymentMethodRequestForIO() {
        return new PaymentMethodRequestDto()
                .clientId(getClientIdIO())
                .name(TEST_NAME)
                .description(TEST_DESC)
                .status(TEST_STATUS)
                .paymentTypeCode(TEST_TYPE_CODE)
                .ranges(List.of(new RangeDto().max(100L).min(0L)))
                .asset(TEST_ASSET)
                .methodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE);
    }

    public static PaymentMethodResponseDto getPaymentMethodResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponseDto()
                .id(paymentMethod.getPaymentMethodID().value().toString())
                .name(paymentMethod.getPaymentMethodName().value())
                .asset(paymentMethod.getPaymentMethodAsset().value())
                .description(paymentMethod.getPaymentMethodDescription().value())
                .status(
                        PaymentMethodStatusDto
                                .fromValue(paymentMethod.getPaymentMethodStatus().value().getCode())
                )
                .paymentTypeCode(paymentMethod.getPaymentMethodTypeCode().value())
                .ranges(List.of(new RangeDto().min(0L).max(100L)))
                .methodManagement(paymentMethod.getPaymentMethodManagement().value());
    }

    public static PaymentMethodsResponseDto getPaymentMethodsResponse(PaymentMethod... paymentMethod) {
        List<PaymentMethodResponseDto> paymentMethods = Arrays.asList(paymentMethod)
                .stream()
                .map(TestUtil::getPaymentMethodResponse)
                .toList();
        return new PaymentMethodsResponseDto()
                .paymentMethods(paymentMethods);
    }

    public static long getTestAmount() {
        return TEST_AMOUNT;
    }

    public static String getTestLang() {
        return TEST_LANG;
    }

    public static String getTestPaymentType() {
        return TEST_TYPE_CODE;
    }

    /*
     * public static ServicesDto getTestServices() { return new ServicesDto()
     * .services( List.of( new ServiceDto() .abiCode("TEST")
     * .channelCode("CHANNEL=") .languageCode(ServiceDto.LanguageCodeEnum.IT)
     * .conventionCode("TEST") .pspCode("TEST") .paymentTypeCode("PO")
     * .pspBusinessName("TEST") .brokerPspCode("TEST") .serviceName("TEST")
     * .serviceDescription("TEST") .minimumAmount(0.0) .maximumAmount(100.0)
     * .fixedCost(100.0) ) ) .pageInfo( new PageInfoDto() .totalPages(1) .limit(50)
     * .page(0) .itemsFound(1) ); }
     *
     */
    public static PaymentMethodDocument getTestPaymentDoc(PaymentMethod paymentMethod) {
        return new PaymentMethodDocument(
                paymentMethod.getPaymentMethodID().value().toString(),
                paymentMethod.getPaymentMethodName().value(),
                paymentMethod.getPaymentMethodDescription().value(),
                paymentMethod.getPaymentMethodStatus().value().toString(),
                paymentMethod.getPaymentMethodAsset().value(),
                paymentMethod.getPaymentMethodRanges().stream().map(r -> Pair.of(r.min(), r.max()))
                        .collect(Collectors.toList()),
                paymentMethod.getPaymentMethodTypeCode().value(),
                paymentMethod.getClientIdEnum().getValue(),
                paymentMethod.getPaymentMethodManagement().value().getValue(),
                paymentMethod.getPaymentMethodBrandAsset().brandAssets().orElse(null)
        );
    }

    public static it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto getBundleOptionDtoClientResponse() {
        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transferList = new ArrayList<>();
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTest")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idCiBundle("idCiBundleTest")
                        .idPsp("idPspTest")
                        .onUs(true)
                        .paymentMethod("idPaymentMethodTest")
                        .primaryCiIncurredFee(BigInteger.ZERO.longValue())
                        .taxPayerFee(BigInteger.ZERO.longValue())
                        .touchpoint("CHECKOUT")
        );
        return new it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto()
                .belowThreshold(true)
                .bundleOptions(
                        transferList
                );
    }

    public static it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto getBundleOptionWithAnyValueDtoClientResponse() {
        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transferList = new ArrayList<>();
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTest")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idCiBundle("idCiBundleTest")
                        .idPsp("idPspTest")
                        .onUs(true)
                        .paymentMethod(null)
                        .primaryCiIncurredFee(BigInteger.ZERO.longValue())
                        .taxPayerFee(BigInteger.ZERO.longValue())
                        .touchpoint("CHECKOUT")
        );
        return new it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto()
                .belowThreshold(true)
                .bundleOptions(
                        transferList
                );
    }

    public static CalculateFeeResponseDto getCalculateFeeResponseFromClientResponse(
                                                                                    it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto gecResponse
    ) {
        return new CalculateFeeResponseDto()
                .belowThreshold(gecResponse.getBelowThreshold())
                .bundles(
                        gecResponse.getBundleOptions() != null ? gecResponse.getBundleOptions()
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
                                                .paymentMethod(t.getPaymentMethod())
                                                .primaryCiIncurredFee(t.getPrimaryCiIncurredFee())
                                                .taxPayerFee(t.getTaxPayerFee())
                                                .touchpoint(t.getTouchpoint())
                                                .pspBusinessName(t.getPspBusinessName())
                                ).collect(Collectors.toList()) : new ArrayList<>()
                );
    }

    public static it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto getPaymentOptionRequestClient() {
        return new it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto()
                .paymentAmount(BigInteger.TEN.longValue())
                .paymentMethod("paymentMethodID")
                .primaryCreditorInstitution("CF")
                .bin("BIN_TEST")
                .touchpoint("CHECKOUT")
                .idPspList(
                        new ArrayList<>(
                                List.of(
                                        new PspSearchCriteriaDto().idPsp("firstPspId"),
                                        new PspSearchCriteriaDto().idPsp("secondPspId")
                                )
                        )
                )
                .transferList(
                        new ArrayList<>(
                                List.of(
                                        new it.pagopa.generated.ecommerce.gec.v1.dto.TransferListItemDto()
                                                .transferCategory("category")
                                                .creditorInstitution("creditorInstitution")
                                                .digitalStamp(true)
                                )
                        )
                );
    }

    public static CalculateFeeRequestDto getCalculateFeeRequest() {
        return new CalculateFeeRequestDto()
                .paymentAmount(BigInteger.TEN.longValue())
                .primaryCreditorInstitution("CF")
                .bin("BIN_TEST")
                .touchpoint("CHECKOUT")
                .addIdPspListItem("string")
                .idPspList(new ArrayList<>(List.of("first", "second")))
                .transferList(
                        new ArrayList<>(
                                List.of(
                                        new TransferListItemDto()
                                                .transferCategory("category")
                                                .creditorInstitution("creditorInstitution")
                                                .digitalStamp(true)
                                )
                        )
                ).isAllCCP(false);
    }

    public static FieldsDto npgResponse() {
        return new FieldsDto()
                .sessionId("sessionId")
                .url("url")
                .state(WorkflowStateDto.CARD_DATA_COLLECTION)
                .securityToken("securityToken")
                .fields(
                        List.of(
                                new FieldDto().id("fieldId1").type("field1Type").src("fieldId1Src")
                                        .propertyClass("fieldId1PropertyClass")
                        )
                );
    }

    public static CardDataResponseDto npgCardDataResponse() {
        return new CardDataResponseDto().bin("12345678").expiringDate("0424").lastFourDigits("1234").circuit("VISA");
    }

    public static NpgSessionDocument npgSessionDocument(
                                                        String orderId,
                                                        String correlationId,
                                                        String sessionId,
                                                        boolean hasCardDataInformation,
                                                        String transactionId
    ) {
        NpgSessionDocument document;
        if (hasCardDataInformation) {
            document = new NpgSessionDocument(
                    orderId,
                    correlationId,
                    sessionId,
                    "securityToken",
                    new CardDataDocument("12345678", "1234", "0424", "VISA"),
                    transactionId
            );
        } else {
            document = new NpgSessionDocument(orderId, correlationId, sessionId, "securityToken", null, transactionId);
        }
        return document;
    }

    public static PatchSessionRequestDto patchSessionRequest() {
        return new PatchSessionRequestDto().transactionId("transactionId");
    }

    public static NpgSessionDocument patchSessionResponse(
                                                          NpgSessionDocument document,
                                                          String newTransactionId
    ) {
        return new NpgSessionDocument(
                document.orderId(),
                document.correlationId(),
                document.sessionId(),
                document.securityToken(),
                document.cardData(),
                newTransactionId
        );
    }

    public static CreateSessionResponseDto createSessionResponseDto(String paymentMethodId) {
        return new CreateSessionResponseDto()
                .orderId("orderId")
                .paymentMethodData(
                        new CardFormFieldsDto().paymentMethod(paymentMethodId).form(
                                List.of(
                                        new it.pagopa.ecommerce.payment.methods.server.model.FieldDto()
                                                .type("TEXT")

                                                .id("CARD_NUMBER")
                                                .src(URI.create("http://localhost")).propertyClass("CARD_FIELD")
                                )
                        )
                );

    }

    public static PaymentMethodRequestDto.ClientIdEnum getClientIdCheckout() {
        return PaymentMethodRequestDto.ClientIdEnum.CHECKOUT;
    }

    public static PaymentMethodRequestDto.ClientIdEnum getClientIdIO() {
        return PaymentMethodRequestDto.ClientIdEnum.IO;
    }

    public static it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto getBundleOptionDtoClientResponseWithUnsortedTransferListAllNotOnUs() {
        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transferList = new ArrayList<>();
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestNotOnUs_1")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest1")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestOnUse")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest2")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TEN.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestNotOnUs_2")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest3")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.ONE.longValue())
                        .touchpoint("CHECKOUT")
        );

        return new it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto()
                .belowThreshold(true)
                .bundleOptions(
                        transferList
                );
    }

    public static it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto getBundleOptionDtoClientResponseWithUnsortedTransferListOnlyOneOnUs() {
        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transferList = new ArrayList<>();
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestNotOnUs_1")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest1")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestNotOnUs_2")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest2")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestOnUs")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest3")
                        .onUs(true)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TEN.longValue())
                        .touchpoint("CHECKOUT")
        );
        return new it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto()
                .belowThreshold(true)
                .bundleOptions(
                        transferList
                );
    }

    public static it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto getBundleOptionDtoClientResponseWithUnsortedTransferMixedWithSameFees() {
        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transferList = new ArrayList<>();
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestOnUs_1")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest1")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestOnUs_2")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest2")
                        .onUs(true)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestOnUs_2")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest3")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestNotOnUs_2")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest4")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestNotOnUs_2")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest5")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        transferList.add(
                new it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto().abi("abiTestNotOnUs_2")
                        .bundleDescription("descriptionTest")
                        .bundleName("bundleNameTest")
                        .idBrokerPsp("idBrokerPspTest")
                        .idBundle("idBundleTest")
                        .idChannel("idChannelTest")
                        .idPsp("idPspTest6")
                        .onUs(false)
                        .paymentMethod("idPaymentMethodTest")
                        .taxPayerFee(BigInteger.TWO.longValue())
                        .touchpoint("CHECKOUT")
        );
        return new it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto()
                .belowThreshold(true)
                .bundleOptions(
                        transferList
                );
    }

    public static class V2 {
        public static it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeRequestDto getMultiNoticeFeesRequest() {
            final var notices = LongStream.of(10L, 20L)
                    .mapToObj(
                            amount -> new PaymentNoticeDto()
                                    .paymentAmount(amount)
                                    .primaryCreditorInstitution("CF")
                                    .transferList(
                                            List.of(
                                                    new it.pagopa.ecommerce.payment.methods.v2.server.model.TransferListItemDto()
                                                            .transferCategory("category")
                                                            .creditorInstitution("creditorInstitution")
                                                            .digitalStamp(true)
                                            )
                                    )
                    ).toList();
            return new it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeRequestDto()
                    .bin("BIN_TEST")
                    .touchpoint("CHECKOUT")
                    .addIdPspListItem("string")
                    .idPspList(new ArrayList<>(List.of("first", "second")))
                    .isAllCCP(false)
                    .paymentNotices(notices);
        }

        public static it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeResponseDto getCalculateFeeResponseFromClientResponse(
                                                                                                                                            it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto gecResponse
        ) {
            return new it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeResponseDto()
                    .belowThreshold(gecResponse.getBelowThreshold())
                    .bundles(
                            gecResponse.getBundleOptions() != null ? gecResponse.getBundleOptions()
                                    .stream()
                                    .map(
                                            t -> new it.pagopa.ecommerce.payment.methods.v2.server.model.BundleDto()
                                                    .abi(t.getAbi())
                                                    .bundleDescription(t.getBundleDescription())
                                                    .bundleName(t.getBundleName())
                                                    .idBrokerPsp(t.getIdBrokerPsp())
                                                    .idBundle(t.getIdBundle())
                                                    .idChannel(t.getIdChannel())
                                                    .idPsp(t.getIdPsp())
                                                    .onUs(t.getOnUs())
                                                    .paymentMethod(t.getPaymentMethod())
                                                    .taxPayerFee(t.getTaxPayerFee())
                                                    .touchpoint(t.getTouchpoint())
                                                    .pspBusinessName(t.getPspBusinessName())
                                    ).collect(Collectors.toList()) : new ArrayList<>()
                    );
        }

        public static it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto getBundleOptionDtoClientResponse() {
            List<it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto> transferList = new ArrayList<>();
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTest")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest")
                            .onUs(true)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.ZERO.longValue())
                            .taxPayerFee(BigInteger.ZERO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            return new it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto()
                    .belowThreshold(true)
                    .bundleOptions(
                            transferList
                    );
        }

        public static it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto getBundleOptionDtoClientResponseWithUnsortedTransferListAllNotOnUs() {
            List<it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto> transferList = new ArrayList<>();
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestNotOnUs_1")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest1")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestOnUse")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest2")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TEN.longValue())
                            .taxPayerFee(BigInteger.TEN.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestNotOnUs_2")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest3")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.ONE.longValue())
                            .taxPayerFee(BigInteger.ONE.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );

            return new it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto()
                    .belowThreshold(true)
                    .bundleOptions(
                            transferList
                    );
        }

        public static it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto getBundleOptionDtoClientResponseWithUnsortedTransferListOnlyOneOnUs() {
            List<it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto> transferList = new ArrayList<>();
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestNotOnUs_1")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest1")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestNotOnUs_2")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest2")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestOnUs")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest3")
                            .onUs(true)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TEN.longValue())
                            .taxPayerFee(BigInteger.TEN.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            return new it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto()
                    .belowThreshold(true)
                    .bundleOptions(
                            transferList
                    );
        }

        public static it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto getBundleOptionDtoClientResponseWithUnsortedTransferMixedWithSameFees() {
            List<it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto> transferList = new ArrayList<>();
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestOnUs_1")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest1")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestOnUs_2")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest2")
                            .onUs(true)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestOnUs_2")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest3")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestNotOnUs_2")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest4")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestNotOnUs_2")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest5")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTestNotOnUs_2")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest6")
                            .onUs(false)
                            .paymentMethod("idPaymentMethodTest")
                            .actualPayerFee(BigInteger.TWO.longValue())
                            .taxPayerFee(BigInteger.TWO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            return new it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto()
                    .belowThreshold(true)
                    .bundleOptions(
                            transferList
                    );
        }

        public static it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto getBundleOptionWithAnyValueDtoClientResponse() {
            List<it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto> transferList = new ArrayList<>();
            transferList.add(
                    new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().abi("abiTest")
                            .bundleDescription("descriptionTest")
                            .bundleName("bundleNameTest")
                            .idBrokerPsp("idBrokerPspTest")
                            .idBundle("idBundleTest")
                            .idChannel("idChannelTest")
                            .idPsp("idPspTest")
                            .onUs(true)
                            .paymentMethod(null)
                            .actualPayerFee(BigInteger.ZERO.longValue())
                            .taxPayerFee(BigInteger.ZERO.longValue())
                            .touchpoint("CHECKOUT")
                            .fees(List.of())
            );
            return new it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto()
                    .belowThreshold(true)
                    .bundleOptions(
                            transferList
                    );
        }

        public static it.pagopa.generated.ecommerce.gec.v2.dto.PaymentOptionMultiDto getPaymentMultiNoticeOptionRequestClient() {
            return new it.pagopa.generated.ecommerce.gec.v2.dto.PaymentOptionMultiDto()
                    .paymentNotice(
                            List.of(
                                    new PaymentNoticeItemDto()
                                            .paymentAmount(BigInteger.TEN.longValue())
                                            .primaryCreditorInstitution("creditorInstitution")
                                            .transferList(
                                                    List.of(
                                                            new it.pagopa.generated.ecommerce.gec.v2.dto.TransferListItemDto()
                                                                    .transferCategory("category")
                                                                    .creditorInstitution("creditorInstitution")
                                                                    .digitalStamp(true)
                                                    )
                                            )
                            )
                    )
                    .paymentMethod("paymentMethodID")
                    .bin("BIN_TEST")
                    .idPspList(
                            List.of(
                                    new it.pagopa.generated.ecommerce.gec.v2.dto.PspSearchCriteriaDto()
                                            .idPsp("firstPspId"),
                                    new it.pagopa.generated.ecommerce.gec.v2.dto.PspSearchCriteriaDto()
                                            .idPsp("secondPspId")
                            )
                    )
                    .touchpoint("CHECKOUT");
        }
    }
}
