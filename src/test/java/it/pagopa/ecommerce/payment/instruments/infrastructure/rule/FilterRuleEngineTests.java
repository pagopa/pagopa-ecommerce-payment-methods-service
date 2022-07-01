package it.pagopa.ecommerce.payment.instruments.infrastructure.rule;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PspStatus;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocumentKey;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
<<<<<<< HEAD
class FilterRuleEngineTests {
=======
public class FilterRuleEngineTests {
>>>>>>> 3be15b35752dad40117d3f647f4d91d6c6a5d31d

    private final double NOT_NULL_AMOUNT = 100;
    private final String NOT_NULL_LANGUAGE = "IT";
    private final String NOT_NULL_TYPE = "PO";
    private final String NULL_STRING = null;

    @Mock
    private PspRepository pspRepository;

    @Autowired
    @InjectMocks
    private FilterRuleEngine filterRuleEngine;


    private PspDocument pspDocument = new PspDocument(
            new PspDocumentKey(
                    "PSP_CODE",
                    "PO",
                    "CHANNEL_0",
                    "IT"
            ),
            new PspStatus(PaymentInstrumentStatusEnum.ENABLED).value().getCode(),
            "Test",
            "Test broker",
            "Test description",
            0.0,
            100.0,
            100.0
    );

    String TEST_PAYMENT_TYPE = "PO";
    Integer TEST_AMOUNT = 100;
    String TEST_LANG = "IT";


    @Test
    /*
    * Precondition filter for: no filter
    * Expected behavior: Should call pspRepository findAll()
     */
    void testEmptyFilter(){
        Mockito.when(pspRepository.findAll()).thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(null, NULL_STRING, NULL_STRING).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1)).findAll();
    }

    @Test
        /*
         * Precondition filter for: amount
         * Expected behavior: Should call pspRepository findPspMatchAmount()
         */
    void testAmountFilter(){
        Mockito.when(pspRepository.findPspMatchAmount(TEST_AMOUNT)).thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(TEST_AMOUNT, NULL_STRING, NULL_STRING).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1)).findPspMatchAmount(TEST_AMOUNT);
    }

    @Test
        /*
         * Precondition filter for: language
         * Expected behavior: Should call pspRepository findPspMatchLang()
         */
    void testLangFilter(){
        Mockito.when(pspRepository.findPspMatchLang(TEST_LANG)).thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(null, TEST_LANG, NULL_STRING).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1)).findPspMatchLang(TEST_LANG);
    }

    @Test
        /*
         * Precondition filter for: paymentType
         * Expected behavior: Should call pspRepository findPspMatchType()
         */
    void testPaymentTypeFilter(){
        String TEST_PAYMENT_TYPE = "PO";
        Mockito.when(pspRepository.findPspMatchType(TEST_PAYMENT_TYPE)).thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(null, NULL_STRING, TEST_PAYMENT_TYPE).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1)).findPspMatchType(TEST_PAYMENT_TYPE);
    }

    @Test
        /*
         * Precondition filter for: amount & lang
         * Expected behavior: Should call pspRepository findPspMatchAmountLang()
         */
    void testAmountLangFilter(){
        Mockito.when(pspRepository.findPspMatchAmountLang(TEST_AMOUNT, TEST_LANG)).thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(TEST_AMOUNT, TEST_LANG, NULL_STRING).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1))
                .findPspMatchAmountLang(TEST_AMOUNT, TEST_LANG);
    }

    @Test
        /*
         * Precondition filter for: amount & paymentType
         * Expected behavior: Should call pspRepository findPspMatchType()
         */
    void testAmountTypeFilter(){
        Mockito.when(pspRepository.findPspMatchAmountType(TEST_AMOUNT, TEST_PAYMENT_TYPE))
                .thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(TEST_AMOUNT, NULL_STRING, TEST_PAYMENT_TYPE).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1))
                .findPspMatchAmountType(TEST_AMOUNT, TEST_PAYMENT_TYPE);
    }

    @Test
        /*
         * Precondition filter for: lang & paymentType
         * Expected behavior: Should call pspRepository findPspMatchTypeLang()
         */
    void testTypeLangFilter(){
        Mockito.when(pspRepository.findPspMatchTypeLang(TEST_PAYMENT_TYPE, TEST_LANG))
                .thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(null, TEST_LANG, TEST_PAYMENT_TYPE).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1))
                .findPspMatchTypeLang(TEST_PAYMENT_TYPE, TEST_LANG);
    }

    @Test
        /*
         * Precondition filter for: amount & lang & paymentType
         * Expected behavior: Should call pspRepository findPspMatchAmountTypeLang()
         */
    void testAmountTypeLangFilter(){
        Mockito.when(pspRepository.findPspMatchAmountTypeLang(TEST_AMOUNT, TEST_PAYMENT_TYPE, TEST_LANG))
                .thenReturn(Flux.just(pspDocument));
        filterRuleEngine.applyFilter(TEST_AMOUNT, TEST_LANG, TEST_PAYMENT_TYPE).collectList().block();

        Mockito.verify(pspRepository, Mockito.times(1))
                .findPspMatchAmountTypeLang(TEST_AMOUNT, TEST_PAYMENT_TYPE, TEST_LANG);
    }
}
