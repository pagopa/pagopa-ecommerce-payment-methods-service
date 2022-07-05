package it.pagopa.ecommerce.payment.instruments.controller;

import it.pagopa.ecommerce.payment.instruments.application.CategoryService;
import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentService;
import it.pagopa.ecommerce.payment.instruments.application.PspService;
import it.pagopa.ecommerce.payment.instruments.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrument;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentCategory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.server.model.*;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.PageInfoDto;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServiceDto;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PaymentInstrumentsController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
public class PaymentInstrumentsControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private PaymentInstrumentService paymentInstrumentService;

    @MockBean
    private PspService pspService;

    @MockBean
    private ApiConfigClient apiConfigClient;

    @Test
    public void shouldCreateNewInstrument(){
        String TEST_NAME = "Test";
        String TEST_DESC = "test";
        PaymentInstrumentRequestDto.StatusEnum TEST_STATUS = PaymentInstrumentRequestDto.StatusEnum.ENABLED;
        UUID TEST_CAT = UUID.randomUUID();

        PaymentInstrumentRequestDto paymentInstrumentRequestDto = new PaymentInstrumentRequestDto()
                .name(TEST_NAME)
                .description(TEST_DESC)
                .status(TEST_STATUS)
                .categoryId(TEST_CAT.toString());

        PaymentInstrument paymentInstrument = new PaymentInstrument(
                new PaymentInstrumentID(UUID.randomUUID()),
                new PaymentInstrumentName("Test"),
                new PaymentInstrumentDescription("Test"),
                new PaymentInstrumentStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentInstrumentType("PO"))
        );

        Mockito.when(paymentInstrumentService.createPaymentInstrument(TEST_NAME, TEST_DESC, TEST_CAT.toString()))
                .thenReturn(Mono.just(paymentInstrument));

        Mockito.when(categoryService.getCategory(TEST_CAT.toString())).thenReturn(Mono.just(
                new PaymentInstrumentCategory(
                        new PaymentInstrumentCategoryID(TEST_CAT),
                        List.of(new PaymentInstrumentType("PO")),
                        new PaymentInstrumentCategoryName("Test")
                )
        ));

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentInstrumentID().value().toString())
                .name(paymentInstrument.getPaymentInstrumentName().value())
                .description(paymentInstrument.getPaymentInstrumentDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentInstrumentStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentInstrumentType::value).collect(Collectors.toList())));

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
        String TEST_NAME = "Test";
        String TEST_DESC = "test";
        PaymentInstrumentRequestDto.StatusEnum TEST_STATUS = PaymentInstrumentRequestDto.StatusEnum.ENABLED;
        UUID TEST_CAT = UUID.randomUUID();

        PaymentInstrument paymentInstrument = new PaymentInstrument(
                new PaymentInstrumentID(UUID.randomUUID()),
                new PaymentInstrumentName("Test"),
                new PaymentInstrumentDescription("Test"),
                new PaymentInstrumentStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentInstrumentType("PO"))
        );

        Mockito.when(paymentInstrumentService.retrivePaymentInstruments(TEST_CAT.toString())).thenReturn(
                Flux.just(paymentInstrument)
        );

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentInstrumentID().value().toString())
                .name(paymentInstrument.getPaymentInstrumentName().value())
                .description(paymentInstrument.getPaymentInstrumentDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentInstrumentStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentInstrumentType::value).collect(Collectors.toList())));

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
        String TEST_NAME = "Test";
        String TEST_DESC = "test";
        PaymentInstrumentRequestDto.StatusEnum TEST_STATUS = PaymentInstrumentRequestDto.StatusEnum.ENABLED;
        UUID TEST_CAT = UUID.randomUUID();

        PaymentInstrument paymentInstrument = new PaymentInstrument(
                new PaymentInstrumentID(UUID.randomUUID()),
                new PaymentInstrumentName("Test"),
                new PaymentInstrumentDescription("Test"),
                new PaymentInstrumentStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentInstrumentType("PO"))
        );

        Mockito.when(paymentInstrumentService.retrivePaymentInstrumentById(
                paymentInstrument.getPaymentInstrumentID().value().toString())
        ).thenReturn(Mono.just(paymentInstrument));

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentInstrumentID().value().toString())
                .name(paymentInstrument.getPaymentInstrumentName().value())
                .description(paymentInstrument.getPaymentInstrumentDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentInstrumentStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentInstrumentType::value).collect(Collectors.toList())));

        webClient
                .get()
                .uri("/payment-instruments/"+paymentInstrument.getPaymentInstrumentID().value())
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
        String TEST_NAME = "Test";
        String TEST_DESC = "test";
        PaymentInstrumentRequestDto.StatusEnum TEST_STATUS = PaymentInstrumentRequestDto.StatusEnum.ENABLED;
        UUID TEST_CAT = UUID.randomUUID();

        PaymentInstrumentRequestDto paymentInstrumentRequestDto = new PaymentInstrumentRequestDto()
                .name(TEST_NAME)
                .description(TEST_DESC)
                .status(TEST_STATUS)
                .categoryId(TEST_CAT.toString());

        PaymentInstrument paymentInstrument = new PaymentInstrument(
                new PaymentInstrumentID(UUID.randomUUID()),
                new PaymentInstrumentName("Test"),
                new PaymentInstrumentDescription("Test"),
                new PaymentInstrumentStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(TEST_CAT),
                new PaymentInstrumentCategoryName("Test"),
                List.of(new PaymentInstrumentType("PO"))
        );

        Mockito.when(paymentInstrumentService.patchPaymentInstrument(
                paymentInstrument.getPaymentInstrumentID().value().toString(), PaymentInstrumentStatusEnum.ENABLED)
        ).thenReturn(Mono.just(paymentInstrument));

        PaymentInstrumentResponseDto expectedResult = new PaymentInstrumentResponseDto()
                .id(paymentInstrument.getPaymentInstrumentID().value().toString())
                .name(paymentInstrument.getPaymentInstrumentName().value())
                .description(paymentInstrument.getPaymentInstrumentDescription().value())
                .status(PaymentInstrumentResponseDto.StatusEnum.fromValue(paymentInstrument.getPaymentInstrumentStatus().value().getCode()))
                .category(new CategoryDto()
                        .id(paymentInstrument.getPaymentInstrumentCategoryID().value().toString())
                        .name(paymentInstrument.getPaymentInstrumentCategoryName().value())
                        .paymentTypeCodes(paymentInstrument.getPaymentInstrumentCategoryTypes()
                                .stream().map(PaymentInstrumentType::value).collect(Collectors.toList())));

        PatchPaymentInstrumentRequestDto patchRequest = new PatchPaymentInstrumentRequestDto()
                .status(PatchPaymentInstrumentRequestDto.StatusEnum.ENABLED);

        webClient
                .patch().uri("/payment-instruments/"+paymentInstrument.getPaymentInstrumentID().value())
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


}
