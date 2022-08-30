package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

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

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class PspFactoryTests {

    @Mock
    private PspRepository pspRepository;

    @InjectMocks
    private PspFactory pspFactory;

    @Test
    void testNewPsp(){
        Psp psp = TestUtil.getTestPsp();

        Mockito.when(pspRepository.findByPspDocumentKey(
                psp.pspCode().value(),
                psp.getPspPaymentMethodType().value(),
                psp.getPspChannelCode().value()
        )).thenReturn(
                Flux.just(TestUtil.getTestPspDoc(psp))
        );

        pspFactory.newPsp(
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
        );
    }
}
