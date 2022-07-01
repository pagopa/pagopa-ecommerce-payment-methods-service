package it.pagopa.ecommerce.payment.instruments.service;

import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentService;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrument;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentFactory;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentInstrumentServiceTests {

    @Mock
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @Mock
    private PaymentInstrumentFactory paymentInstrumentFactory;

    @InjectMocks
    private PaymentInstrumentService paymentInstrumentService;

    @Test
    void shouldCreatePaymentInstrument(){
        String TEST_NAME = "test";
        String TEST_DESC = "desc";
        String TEST_CAT_ID = UUID.randomUUID().toString();

        /*
        PaymentInstrument paymentInstrument = paymentInstrumentService.createPaymentInstrument(
                TEST_NAME,
                TEST_DESC,
                TEST_CAT_ID
        ).block();
         */

    }

    @Test
    void shouldRetrievePaymentInstruments(){}

    @Test
    void shouldPatchPaymentInstrument(){}

    @Test
    void shouldRetrivePaymentInstrumentById(){}
}
