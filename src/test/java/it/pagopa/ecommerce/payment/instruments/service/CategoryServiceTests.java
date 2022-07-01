package it.pagopa.ecommerce.payment.instruments.service;

import it.pagopa.ecommerce.payment.instruments.application.CategoryService;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentCategory;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentCategoryFactory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryID;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryName;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentType;
import it.pagopa.ecommerce.payment.instruments.exception.CategoryAlreadyInUseException;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

    @Mock
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @Mock
    private PaymentInstrumentCategoryFactory categoryFactory;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldReturnEmptyFluxCategories() {
        Mockito.when(paymentInstrumentCategoryRepository.findAll()).thenReturn(Flux.empty());

        List<PaymentInstrumentCategory> categories = categoryService.getCategories().collectList().block();

        assert categories != null;
        assertEquals(0, categories.size());
    }

    @Test
    void shouldReturnCategory() {

        PaymentInstrumentCategoryDocument categoryDocument = new PaymentInstrumentCategoryDocument(
                UUID.randomUUID().toString(),
                "test",
                List.of("PO")
        );

        Mockito.when(paymentInstrumentCategoryRepository.findAll()).thenReturn(Flux.just(categoryDocument));

        List<PaymentInstrumentCategory> categories = categoryService.getCategories().collectList().block();

        assert categories != null;
        assertEquals(1, categories.size());
        assertEquals(categoryDocument.getPaymentInstrumentCategoryID(),
                categories.get(0).getPaymentInstrumentCategoryID().value().toString());
    }

    @Test
    void shouldReturnCategoryById() {

        PaymentInstrumentCategoryDocument categoryDocument = new PaymentInstrumentCategoryDocument(
                UUID.randomUUID().toString(),
                "test",
                List.of("PO")
        );

        Mockito.when(paymentInstrumentCategoryRepository.findById(categoryDocument.getPaymentInstrumentCategoryID()))
                .thenReturn(Mono.just(categoryDocument));

        PaymentInstrumentCategory category = categoryService.getCategory(categoryDocument.getPaymentInstrumentCategoryID()).block();

        assertNotNull(category);
        assertEquals(categoryDocument.getPaymentInstrumentCategoryID(),
                category.getPaymentInstrumentCategoryID().value().toString());
    }

    @Test
    void shouldReturnCategoryByName() {

        PaymentInstrumentCategoryDocument categoryDocument = new PaymentInstrumentCategoryDocument(
                UUID.randomUUID().toString(),
                "test",
                List.of("PO")
        );

        Mockito.when(paymentInstrumentCategoryRepository.findByPaymentInstrumentCategoryName(categoryDocument.getPaymentInstrumentCategoryName()))
                .thenReturn(Mono.just(categoryDocument));

        PaymentInstrumentCategory category = categoryService.getCategoryByName(categoryDocument.getPaymentInstrumentCategoryName()).block();

        assertNotNull(category);
        assertEquals(categoryDocument.getPaymentInstrumentCategoryID(),
                category.getPaymentInstrumentCategoryID().value().toString());
    }

    @Test
    void shouldCreateCategory() {
        String TEST_NAME = "test_name";
        List<String> TEST_TYPES = List.of("PO");

        PaymentInstrumentCategoryDocument categoryDocument = new PaymentInstrumentCategoryDocument(
                UUID.randomUUID().toString(),
                TEST_NAME,
                TEST_TYPES
        );
        PaymentInstrumentCategory expectedPaymentInstrumentCategory = new PaymentInstrumentCategory(
                new PaymentInstrumentCategoryID(UUID.fromString(categoryDocument.getPaymentInstrumentCategoryID())),
                List.of(new PaymentInstrumentType(TEST_TYPES.get(0))),
                new PaymentInstrumentCategoryName(TEST_NAME));

        Mockito.when(paymentInstrumentCategoryRepository.save(Mockito.any())).thenReturn(Mono.just(categoryDocument));
        Mockito.when(categoryFactory.newCategory(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        )).thenReturn(Mono.just(expectedPaymentInstrumentCategory));

        PaymentInstrumentCategory paymentInstrumentCategory = categoryService.createCategory(TEST_NAME, TEST_TYPES)
                .block();

        assert paymentInstrumentCategory != null;

        assertEquals(categoryDocument.getPaymentInstrumentCategoryID(),
                paymentInstrumentCategory.getPaymentInstrumentCategoryID().value().toString());

        assertEquals(categoryDocument.getPaymentInstrumentCategoryName(),
                paymentInstrumentCategory.getPaymentInstrumentCategoryName().value());

        assertEquals(categoryDocument.getPaymentInstrumentCategoryTypes().get(0),
                paymentInstrumentCategory.getPaymentInstrumentTypes().get(0).value());

    }

    @Test
    void shouldUpdateCategory() {
        String TEST_ID = UUID.randomUUID().toString();
        String TEST_NAME = "test_name";
        List<String> TEST_TYPES = List.of("PO");

        PaymentInstrumentCategoryDocument categoryDocument = new PaymentInstrumentCategoryDocument(
                TEST_ID,
                TEST_NAME,
                TEST_TYPES
        );
        PaymentInstrumentCategory expectedPaymentInstrumentCategory = new PaymentInstrumentCategory(
                new PaymentInstrumentCategoryID(UUID.fromString(categoryDocument.getPaymentInstrumentCategoryID())),
                List.of(new PaymentInstrumentType(TEST_TYPES.get(0))),
                new PaymentInstrumentCategoryName(TEST_NAME));

        Mockito.when(paymentInstrumentCategoryRepository.save(Mockito.any())).thenReturn(Mono.just(categoryDocument));
        Mockito.when(paymentInstrumentCategoryRepository.findByPaymentInstrumentCategoryName(TEST_NAME))
                .thenReturn(Mono.empty());
        Mockito.when(paymentInstrumentCategoryRepository.findById(TEST_ID)).thenReturn(Mono.just(categoryDocument));

        PaymentInstrumentCategory paymentInstrumentCategory = categoryService.updateCategory(TEST_ID, TEST_NAME, TEST_TYPES)
                .block();

        assert paymentInstrumentCategory != null;

        assertEquals(categoryDocument.getPaymentInstrumentCategoryID(),
                paymentInstrumentCategory.getPaymentInstrumentCategoryID().value().toString());

        assertEquals(categoryDocument.getPaymentInstrumentCategoryName(),
                paymentInstrumentCategory.getPaymentInstrumentCategoryName().value());

        assertEquals(categoryDocument.getPaymentInstrumentCategoryTypes().get(0),
                paymentInstrumentCategory.getPaymentInstrumentTypes().get(0).value());

    }

    @Test
    void shouldFailUpdateCategoryDuplicatedName() {
        String TEST_ID = UUID.randomUUID().toString();
        String TEST_ANOTHER_ID = UUID.randomUUID().toString();
        String TEST_NAME = "test_name";
        List<String> TEST_TYPES = List.of("PO");

        PaymentInstrumentCategoryDocument categoryDocument = new PaymentInstrumentCategoryDocument(
                TEST_ID,
                TEST_NAME,
                TEST_TYPES
        );

        PaymentInstrumentCategoryDocument duplicatedDocument = new PaymentInstrumentCategoryDocument(
                TEST_ANOTHER_ID,
                TEST_NAME,
                TEST_TYPES
        );

        PaymentInstrumentCategory expectedPaymentInstrumentCategory = new PaymentInstrumentCategory(
                new PaymentInstrumentCategoryID(UUID.fromString(categoryDocument.getPaymentInstrumentCategoryID())),
                List.of(new PaymentInstrumentType(TEST_TYPES.get(0))),
                new PaymentInstrumentCategoryName(TEST_NAME));

        Mockito.when(paymentInstrumentCategoryRepository.save(Mockito.any())).thenReturn(Mono.just(categoryDocument));
        Mockito.when(paymentInstrumentCategoryRepository.findByPaymentInstrumentCategoryName(TEST_NAME))
                .thenReturn(Mono.just(duplicatedDocument));
        Mockito.when(paymentInstrumentCategoryRepository.findById(TEST_ID)).thenReturn(Mono.just(categoryDocument));

        assertThrows(CategoryAlreadyInUseException.class, () ->
                categoryService.updateCategory(TEST_ID, TEST_NAME, TEST_TYPES)
                        .block()
        );
    }

    @Test
    void shouldUpdateCategorySameName() {
        String TEST_ID = UUID.randomUUID().toString();
        String TEST_NAME = "test_name";
        List<String> TEST_TYPES = List.of("PO");
        List<String> TEST_NEW_TYPES = List.of("PO", "PP");

        PaymentInstrumentCategoryDocument categoryDocument = new PaymentInstrumentCategoryDocument(
                TEST_ID,
                TEST_NAME,
                TEST_TYPES
        );


        Mockito.when(paymentInstrumentCategoryRepository.save(Mockito.any())).thenReturn(Mono.just(categoryDocument));
        Mockito.when(paymentInstrumentCategoryRepository.findByPaymentInstrumentCategoryName(TEST_NAME))
                .thenReturn(Mono.just(categoryDocument));
        Mockito.when(paymentInstrumentCategoryRepository.findById(TEST_ID)).thenReturn(Mono.just(categoryDocument));

        PaymentInstrumentCategory category = categoryService.updateCategory(TEST_ID, TEST_NAME, TEST_NEW_TYPES).block();

        assertEquals(categoryDocument.getPaymentInstrumentCategoryTypes().get(0),
                category.getPaymentInstrumentTypes().get(0).value());
    }

}
