package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.exception.PspAlreadyInUseException;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.instruments.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PspFactoryTests {

    @Mock
    private PspRepository pspRepository;

    @InjectMocks
    private PspFactory pspFactory;

    @Test
    void shouldThrowPspAlreadyInUse(){
        Psp psp = TestUtil.getTestPsp();

        Mockito.when(pspRepository.findByPspDocumentKey(
                any(),
                any(),
                any()
        )).thenReturn(
                Flux.just(TestUtil.getTestPspDoc(psp))
        );

        assertThrows(PspAlreadyInUseException.class,
                () -> pspFactory.newPsp(
                        psp.getPspCode(),
                        psp.getPspPaymentMethodType(),
                        psp.getPspStatus(),
                        psp.getPspBusinessName(),
                        psp.getPspBrokerName(),
                        psp.getPspDescription(),
                        psp.getPspLanguage(),
                        psp.getPspMinAmount(),
                        psp.getPspMaxAmount(),
                        psp.getPspChannelCode(),
                        psp.getPspFixedCost()
                ).block());
    }
    @Test
    void shouldCreatePsp(){
        Psp psp = TestUtil.getTestPsp();

        Mockito.when(pspRepository.findByPspDocumentKey(
                any(),
                any(),
                any()
        )).thenReturn(
                Flux.empty()
        );


       Psp res = pspFactory.newPsp(
               psp.getPspCode(),
                psp.getPspPaymentMethodType(),
                psp.getPspStatus(),
                psp.getPspBusinessName(),
                psp.getPspBrokerName(),
                psp.getPspDescription(),
                psp.getPspLanguage(),
                psp.getPspMinAmount(),
                psp.getPspMaxAmount(),
                psp.getPspChannelCode(),
                psp.getPspFixedCost()).block();

       assertEquals(psp.getPspCode(), res.getPspCode());
    }
}
