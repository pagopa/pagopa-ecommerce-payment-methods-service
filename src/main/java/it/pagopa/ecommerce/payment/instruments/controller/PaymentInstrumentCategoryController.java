package it.pagopa.ecommerce.payment.instruments.controller;

import it.pagopa.ecommerce.payment.instruments.application.CategoryService;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentType;
import it.pagopa.ecommerce.payment.instruments.server.api.PaymentInstrumentCategoriesApi;
import it.pagopa.ecommerce.payment.instruments.server.model.CategoriesResponseDto;
import it.pagopa.ecommerce.payment.instruments.server.model.CategoryDto;
import it.pagopa.ecommerce.payment.instruments.server.model.CategoryRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PaymentInstrumentCategoryController implements PaymentInstrumentCategoriesApi {
    @Autowired
    private CategoryService categoryService;

    @Override
    public Mono<ResponseEntity<CategoriesResponseDto>> getCategories(ServerWebExchange exchange) {
        return categoryService.getCategories().collectList().map(
                categories -> {
                    List<CategoryDto> categoryDtos = categories.stream().map(
                            c -> new CategoryDto()
                                    .id(c.getPaymentInstrumentCategoryID().value().toString())
                                    .name(c.getPaymentInstrumentCategoryName().value())
                                    .paymentTypeCodes(c.getPaymentInstrumentTypes().stream().map(PaymentInstrumentType::value).collect(Collectors.toList()))
                    ).collect(Collectors.toList());

                    return ResponseEntity.ok(new CategoriesResponseDto().categories(categoryDtos));
                }
        );
    }

    @Override
    public Mono<ResponseEntity<CategoryDto>> getCategoryByID(String id, ServerWebExchange exchange) {
        return categoryService.getCategory(id).map(
                category -> {
                    CategoryDto categoryDto = new CategoryDto()
                            .id(category.getPaymentInstrumentCategoryID().value().toString())
                            .name(category.getPaymentInstrumentCategoryName().value())
                            .paymentTypeCodes(category.getPaymentInstrumentTypes().stream().map(PaymentInstrumentType::value)
                                    .collect(Collectors.toList()));

                    return ResponseEntity.ok(categoryDto);
                }
        );
    }

    @Override
    public Mono<ResponseEntity<CategoryDto>> addCategory(Mono<CategoryRequestDto> categoryRequestDto, ServerWebExchange exchange) {
        return categoryRequestDto.flatMap(
                request -> categoryService.createCategory(
                                request.getName(), request.getPaymentTypeCodes())
                        .map(category -> {
                            CategoryDto categoryDto = new CategoryDto()
                                    .id(category.getPaymentInstrumentCategoryID().value().toString())
                                    .name(category.getPaymentInstrumentCategoryName().value())
                                    .paymentTypeCodes(category.getPaymentInstrumentTypes().stream().map(PaymentInstrumentType::value)
                                            .collect(Collectors.toList()));

                            return ResponseEntity.ok(categoryDto);
                        })
        );
    }


    @Override
    public Mono<ResponseEntity<CategoryDto>> patchCategory(String id, Mono<CategoryRequestDto> categoryRequestDto, ServerWebExchange exchange) {
        return categoryRequestDto.flatMap(
                req -> categoryService.updateCategory(id, req.getName(), req.getPaymentTypeCodes())
                        .map(c -> {
                            CategoryDto categoryDto = new CategoryDto()
                                    .id(c.getPaymentInstrumentCategoryID().value().toString())
                                    .name(c.getPaymentInstrumentCategoryName().value())
                                    .paymentTypeCodes(c.getPaymentInstrumentTypes().stream().map(PaymentInstrumentType::value)
                                            .collect(Collectors.toList()));

                            return ResponseEntity.ok(categoryDto);
                        })
        );
    }
}
