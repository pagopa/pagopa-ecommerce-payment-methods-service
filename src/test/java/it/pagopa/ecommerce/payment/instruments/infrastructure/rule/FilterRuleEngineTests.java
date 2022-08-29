package it.pagopa.ecommerce.payment.instruments.infrastructure.rule;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PspStatus;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocumentKey;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentMethodStatusEnum;
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
class FilterRuleEngineTests {

    private final double NOT_NULL_AMOUNT = 100;
    private final String NOT_NULL_LANGUAGE = "IT";
    private final String NOT_NULL_TYPE = "PO";

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
            new PspStatus(PaymentMethodStatusEnum.ENABLED).value().getCode(),
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

    String TEST_ID = "ID";


//    @Test
//    /*
//    * Precondition filter for: no filter
//    * Expected behavior: Should call pspRepository findAll()
//     */
//    void testEmptyFilter(){
//        Mockito.when(pspRepository.findAll()).thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, null, null, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1)).findAll();
//    }
//
//    @Test
//        /*
//         * Precondition filter for: amount
//         * Expected behavior: Should call pspRepository findPspMatchAmount()
//         */
//    void testAmountFilter(){
//        Mockito.when(pspRepository.findPspMatchAmount(TEST_AMOUNT)).thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, TEST_AMOUNT, null, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1)).findPspMatchAmount(TEST_AMOUNT);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: language
//         * Expected behavior: Should call pspRepository findPspMatchLang()
//         */
//    void testLangFilter(){
//        Mockito.when(pspRepository.findPspMatchLang(TEST_LANG)).thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, null, TEST_LANG, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1)).findPspMatchLang(TEST_LANG);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentType
//         * Expected behavior: Should call pspRepository findPspMatchType()
//         */
//    void testPaymentTypeFilter(){
//        String TEST_PAYMENT_TYPE = "PO";
//        Mockito.when(pspRepository.findPspMatchType(TEST_PAYMENT_TYPE)).thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, null, null, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1)).findPspMatchType(TEST_PAYMENT_TYPE);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: amount & lang
//         * Expected behavior: Should call pspRepository findPspMatchAmountLang()
//         */
//    void testAmountLangFilter(){
//        Mockito.when(pspRepository.findPspMatchAmountLang(TEST_AMOUNT, TEST_LANG)).thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, TEST_AMOUNT, TEST_LANG, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchAmountLang(TEST_AMOUNT, TEST_LANG);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: amount & paymentType
//         * Expected behavior: Should call pspRepository findPspMatchType()
//         */
//    void testAmountTypeFilter(){
//        Mockito.when(pspRepository.findPspMatchAmountType(TEST_AMOUNT, TEST_PAYMENT_TYPE))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, TEST_AMOUNT, null, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchAmountType(TEST_AMOUNT, TEST_PAYMENT_TYPE);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: lang & paymentType
//         * Expected behavior: Should call pspRepository findPspMatchTypeLang()
//         */
//    void testTypeLangFilter(){
//        Mockito.when(pspRepository.findPspMatchTypeLang(TEST_PAYMENT_TYPE, TEST_LANG))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, null, TEST_LANG, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchTypeLang(TEST_PAYMENT_TYPE, TEST_LANG);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: amount & lang & paymentType
//         * Expected behavior: Should call pspRepository findPspMatchAmountTypeLang()
//         */
//    void testAmountTypeLangFilter(){
//        Mockito.when(pspRepository.findPspMatchAmountTypeLang(TEST_AMOUNT, TEST_PAYMENT_TYPE, TEST_LANG))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(null, TEST_AMOUNT, TEST_LANG, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchAmountTypeLang(TEST_AMOUNT, TEST_PAYMENT_TYPE, TEST_LANG);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId
//         * Expected behavior: Should call pspRepository findPspMatchId()
//         */
//    void testIdFilter(){
//        Mockito.when(pspRepository.findPspMatchId(TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, null, null, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchId(TEST_ID);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId, amount
//         * Expected behavior: Should call pspRepository findPspMatchAmountId
//         */
//    void testAmountIdFilter(){
//        Mockito.when(pspRepository.findPspMatchAmountId(TEST_AMOUNT, TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, TEST_AMOUNT, null, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchAmountId(TEST_AMOUNT, TEST_ID);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId, lang
//         * Expected behavior: Should call findPspMatchLangId
//         */
//    void testLangIdFilter(){
//        Mockito.when(pspRepository.findPspMatchLangId(TEST_LANG, TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, null, TEST_LANG, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchLangId(TEST_LANG, TEST_ID);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId, paymentType
//         * Expected behavior: Should call findPspMatchTypeId
//         */
//    void testTypeIdFilter(){
//        Mockito.when(pspRepository.findPspMatchTypeId(TEST_PAYMENT_TYPE, TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, null, null, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchTypeId(TEST_PAYMENT_TYPE, TEST_ID);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId, lang, amount
//         * Expected behavior: Should call findPspMatchAmountLangId
//         */
//    void testAmountLangIdFilter(){
//        Mockito.when(pspRepository.findPspMatchAmountLangId(TEST_AMOUNT, TEST_LANG, TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, TEST_AMOUNT, TEST_LANG, null).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchAmountLangId(TEST_AMOUNT, TEST_LANG, TEST_ID);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId, paymentType, amount
//         * Expected behavior: Should call findPspMatchAmountLangId
//         */
//    void testAmountTypeIdFilter(){
//        Mockito.when(pspRepository.findPspMatchAmountTypeId(TEST_AMOUNT, TEST_PAYMENT_TYPE, TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, TEST_AMOUNT, null, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchAmountTypeId(TEST_AMOUNT, TEST_PAYMENT_TYPE, TEST_ID);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId, paymentType, amount
//         * Expected behavior: Should call findPspMatchAmountLangId
//         */
//    void testLangTypeIdFilter(){
//        Mockito.when(pspRepository.findPspMatchLangTypeId(TEST_LANG, TEST_PAYMENT_TYPE, TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, null, TEST_LANG, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchLangTypeId(TEST_LANG, TEST_PAYMENT_TYPE, TEST_ID);
//    }
//
//    @Test
//        /*
//         * Precondition filter for: paymentInstrumentId, paymentType, amount
//         * Expected behavior: Should call findPspMatchAmountLangId
//         */
//    void testAmountLangTypeId(){
//        Mockito.when(pspRepository.findPspMatchAmountLangTypeId(TEST_AMOUNT, TEST_LANG, TEST_PAYMENT_TYPE, TEST_ID))
//                .thenReturn(Flux.just(pspDocument));
//        filterRuleEngine.applyFilter(TEST_ID, TEST_AMOUNT, TEST_LANG, TEST_PAYMENT_TYPE).collectList().block();
//
//        Mockito.verify(pspRepository, Mockito.times(1))
//                .findPspMatchAmountLangTypeId(TEST_AMOUNT, TEST_LANG, TEST_PAYMENT_TYPE, TEST_ID);
//    }
}
