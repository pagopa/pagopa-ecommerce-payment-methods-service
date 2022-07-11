package it.pagopa.ecommerce.payment.instruments.controller;

import it.pagopa.ecommerce.payment.instruments.application.CategoryService;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentCategory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryID;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryName;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentType;
import it.pagopa.ecommerce.payment.instruments.server.model.CategoriesResponseDto;
import it.pagopa.ecommerce.payment.instruments.server.model.CategoryDto;
import it.pagopa.ecommerce.payment.instruments.server.model.CategoryRequestDto;
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
@WebFluxTest(PaymentInstrumentCategoryController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
public class PaymentInstrumentsCategoryControllerTests {

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private CategoryService categoryService;

    @Test
    public void shouldGetCategories() throws Exception {

        Flux<PaymentInstrumentCategory> categoryFlux = Flux.just(
                new PaymentInstrumentCategory(
                        new PaymentInstrumentCategoryID(UUID.randomUUID()),
                        List.of(new PaymentInstrumentType("test type")),
                        new PaymentInstrumentCategoryName("test name"))
        );

        Mockito.when(categoryService.getCategories()).thenReturn(categoryFlux);

        webClient
                .get().uri("/payment-instrument-categories")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CategoriesResponseDto.class);
    }

    @Test
    public void shouldGetCategoryById() {
        UUID categoryId = UUID.randomUUID();
        String testName = "test";
        List<String> testTypes = List.of("test");

        Mono<PaymentInstrumentCategory> categoryMono = Mono.just(
                new PaymentInstrumentCategory(
                        new PaymentInstrumentCategoryID(categoryId),
                        testTypes.stream().map(PaymentInstrumentType::new).collect(Collectors.toList()),
                        new PaymentInstrumentCategoryName(testName))
        );

        Mockito.when(categoryService.getCategory(categoryId.toString())).thenReturn(categoryMono);

        webClient
                .get().uri("/payment-instrument-categories/"+categoryId.toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CategoryDto.class)
                .isEqualTo(new CategoryDto()
                        .id(categoryId.toString())
                        .name(testName)
                        .paymentTypeCodes(testTypes));

    }

    @Test
    public void shouldAddCategory() {
        UUID categoryId = UUID.randomUUID();
        String testName = "test";
        List<String> testTypes = List.of("test");

        Mono<PaymentInstrumentCategory> categoryMono = Mono.just(
                new PaymentInstrumentCategory(
                        new PaymentInstrumentCategoryID(categoryId),
                        testTypes.stream().map(PaymentInstrumentType::new).collect(Collectors.toList()),
                        new PaymentInstrumentCategoryName(testName))
        );

        Mockito.when(categoryService.createCategory(testName, testTypes)).thenReturn(categoryMono);

        CategoryRequestDto categoryRequestDto = new CategoryRequestDto().name(testName).paymentTypeCodes(testTypes);

        webClient
                .post().uri("/payment-instrument-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(categoryRequestDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CategoryDto.class)
                .isEqualTo(new CategoryDto()
                        .id(categoryId.toString())
                        .name(testName)
                        .paymentTypeCodes(testTypes));

    }

    @Test
    public void shouldPatchCategory(){
        UUID categoryId = UUID.randomUUID();
        String testName = "test";
        List<String> testTypes = List.of("test");

        Mono<PaymentInstrumentCategory> categoryMono = Mono.just(
                new PaymentInstrumentCategory(
                        new PaymentInstrumentCategoryID(categoryId),
                        testTypes.stream().map(PaymentInstrumentType::new).collect(Collectors.toList()),
                        new PaymentInstrumentCategoryName(testName))
        );

        Mockito.when(categoryService.updateCategory(categoryId.toString(), testName, testTypes)).thenReturn(categoryMono);

        CategoryRequestDto categoryRequestDto = new CategoryRequestDto().name(testName).paymentTypeCodes(testTypes);

        webClient
                .patch().uri("/payment-instrument-categories/"+categoryId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(categoryRequestDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CategoryDto.class)
                .isEqualTo(new CategoryDto()
                        .id(categoryId.toString())
                        .name(testName)
                        .paymentTypeCodes(testTypes));
    }
}
