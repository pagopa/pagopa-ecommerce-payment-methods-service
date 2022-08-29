package it.pagopa.ecommerce.payment.instruments.controller;

import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentServiceOld;
import it.pagopa.ecommerce.payment.instruments.application.PspService;
import it.pagopa.ecommerce.payment.instruments.client.ApiConfigClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PaymentMethodsController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
public class PaymentMethodsControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private PaymentInstrumentServiceOld paymentInstrumentService;

    @MockBean
    private PspService pspService;

    @MockBean
    private ApiConfigClient apiConfigClient;

    /*
    @Test
    public void shouldCreateNewInstrument(){
        String TEST_NAME = "Test";
        String TEST_DESC = "test";
        PaymentInstrumentRequestDto.StatusEnum TEST_STATUS = PaymentInstrumentRequestDto.StatusEnum.ENABLED;
        UUID TEST_CAT = UUID.randomUUID();
        String TEST_TYPE_CODE = "test";

        PaymentInstrumentRequestDto paymentInstrumentRequestDto = new PaymentInstrumentRequestDto()
                .name(TEST_NAME)
                .description(TEST_DESC)
                .status(TEST_STATUS)
                .categoryId(TEST_CAT.toString())
                .paymentTypeCode(TEST_TYPE_CODE);

        PaymentMethod paymentInstrument = new PaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName("Test"),
                new PaymentMethodDescription("Test"),
                new PaymentMethodStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentMethodType("PO")),
                new PaymentMethodType(TEST_TYPE_CODE)
        );

        Mockito.when(paymentInstrumentService.createPaymentInstrument(TEST_NAME, TEST_DESC, TEST_CAT.toString(), TEST_TYPE_CODE))
                .thenReturn(Mono.just(paymentInstrument));

        Mockito.when(categoryService.getCategory(TEST_CAT.toString())).thenReturn(Mono.just(
                new PaymentInstrumentCategory(
                        new PaymentInstrumentCategoryID(TEST_CAT),
                        List.of(new PaymentMethodType("PO")),
                        new PaymentInstrumentCategoryName("Test")
                )
        ));

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentMethodID().value().toString())
                .name(paymentInstrument.getPaymentMethodName().value())
                .description(paymentInstrument.getPaymentMethodDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentMethodStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentMethodType::value).collect(Collectors.toList())))
                .paymentTypeCode(paymentInstrument.getPaymentMethodTypeCode().value());

        webClient
                .post().uri("/payment-instruments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentInstrumentRequestDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentInstrumentResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    public void shouldGetAllInstruments(){
        PaymentInstrumentRequestDto.StatusEnum TEST_STATUS = PaymentInstrumentRequestDto.StatusEnum.ENABLED;
        UUID TEST_CAT = UUID.randomUUID();

        PaymentMethod paymentInstrument = new PaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName("Test"),
                new PaymentMethodDescription("Test"),
                new PaymentMethodStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentMethodType("PO")),
                new PaymentMethodType("test")
        );

        Mockito.when(paymentInstrumentService.retrivePaymentInstruments(TEST_CAT.toString())).thenReturn(
                Flux.just(paymentInstrument)
        );

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentMethodID().value().toString())
                .name(paymentInstrument.getPaymentMethodName().value())
                .description(paymentInstrument.getPaymentMethodDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentMethodStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentMethodType::value).collect(Collectors.toList())))
                .paymentTypeCode(paymentInstrument.getPaymentMethodTypeCode().value());

        webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/payment-instruments")
                    .queryParam("categoryId", TEST_CAT.toString())
                    .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentInstrumentResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    public void shouldGetPSPs(){
        String TEST_CODE = "001";
        String TEST_NAME = "test";
        String TEST_DESC = "test";
        String TEST_CHANNEL = "channel0";

        PspDto pspDto = new PspDto()
                .code(TEST_CODE)
                .brokerName(TEST_NAME)
                .description(TEST_DESC)
                .businessName(TEST_NAME)
                .status(PspDto.StatusEnum.ENABLED)
                .channelCode(TEST_CHANNEL);

        Mockito.when(pspService.retrievePsps(null, null, null, null))
                        .thenReturn(Flux.just(pspDto));

        PSPsResponseDto expectedResult = new PSPsResponseDto()
                .psp(List.of(pspDto));

        webClient
                .get()
                .uri("/payment-instruments/psps")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PSPsResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    public void shouldGetAnInstrument(){
        UUID TEST_CAT = UUID.randomUUID();

        PaymentMethod paymentInstrument = new PaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName("Test"),
                new PaymentMethodDescription("Test"),
                new PaymentMethodStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentMethodType("PO")),
                new PaymentMethodType("test")
        );

        Mockito.when(paymentInstrumentService.retrivePaymentInstrumentById(
                paymentInstrument.getPaymentMethodID().value().toString())
        ).thenReturn(Mono.just(paymentInstrument));

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentMethodID().value().toString())
                .name(paymentInstrument.getPaymentMethodName().value())
                .description(paymentInstrument.getPaymentMethodDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentMethodStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentMethodType::value).collect(Collectors.toList())))
                .paymentTypeCode(paymentInstrument.getPaymentMethodTypeCode().value());

        webClient
                .get()
                .uri("/payment-instruments/"+paymentInstrument.getPaymentMethodID().value())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentInstrumentResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    public void shouldGetPaymentInstrumentPSPs(){
        String TEST_CODE = "001";
        String TEST_NAME = "test";
        String TEST_DESC = "test";
        String TEST_CHANNEL = "channel0";
        String TEST_INSTRUMENT_ID = UUID.randomUUID().toString();

        PspDto pspDto = new PspDto()
                .code(TEST_CODE)
                .brokerName(TEST_NAME)
                .description(TEST_DESC)
                .businessName(TEST_NAME)
                .status(PspDto.StatusEnum.ENABLED)
                .channelCode(TEST_CHANNEL);

        Mockito.when(pspService.retrievePsps(TEST_INSTRUMENT_ID, null, null, null))
                .thenReturn(Flux.just(pspDto));

        PSPsResponseDto expectedResult = new PSPsResponseDto()
                .psp(List.of(pspDto));

        webClient
                .get()
                .uri("/payment-instruments/"+TEST_INSTRUMENT_ID+"/psps")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PSPsResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldPatchPaymentInstrument(){
        UUID TEST_CAT = UUID.randomUUID();

        PaymentMethod paymentInstrument = new PaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName("Test"),
                new PaymentMethodDescription("Test"),
                new PaymentMethodStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentMethodType("PO")),
                new PaymentMethodType("test")
        );

        Mockito.when(paymentInstrumentService.patchPaymentInstrument(
                paymentInstrument.getPaymentMethodID().value().toString(), PaymentInstrumentStatusEnum.ENABLED)
        ).thenReturn(Mono.just(paymentInstrument));

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentMethodID().value().toString())
                .name(paymentInstrument.getPaymentMethodName().value())
                .description(paymentInstrument.getPaymentMethodDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentMethodStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentMethodType::value).collect(Collectors.toList())))
                .paymentTypeCode(paymentInstrument.getPaymentMethodTypeCode().value());

        PatchPaymentInstrumentRequestDto patchRequest = new PatchPaymentInstrumentRequestDto()
                .status(PatchPaymentInstrumentRequestDto.StatusEnum.ENABLED);

        webClient
                .patch().uri("/payment-instruments/"+paymentInstrument.getPaymentMethodID().value())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patchRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentInstrumentResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    public void shouldScheduleUpdate(){

        Mockito.when(apiConfigClient.getPSPs(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(
                Mono.just(new ServicesDto()
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
                        )
                )
        );

        webClient
                .put().uri("/payment-instruments/psps")
                .exchange()
                .expectStatus()
                .isAccepted();

        Mockito.verify(pspService, Mockito.times(1)).updatePSPs(Mockito.any());
    }
     */
}
