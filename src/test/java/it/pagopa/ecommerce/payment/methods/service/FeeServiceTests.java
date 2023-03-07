package it.pagopa.ecommerce.payment.methods.service;

import it.pagopa.ecommerce.payment.methods.application.FeeService;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentOptionDto;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class FeeServiceTests {

    @Mock
    private AfmClient afmClient;

    @Mock
    private PaymentMethodService paymentMethodService;

    @InjectMocks
    private FeeService feeService;

    @Test
    void shouldRetrieveFee() {
        PaymentOptionDto paymentOptionDtoRequest = TestUtil.getPaymentOptionRequest();
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();

        Mockito.when(paymentMethodService.removeDisabledPsp(Mockito.anyList())).thenReturn(
                Mono.just(gecResponse.getBundleOptions())
        );
        Mockito.when(afmClient.getFees(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(gecResponse));

        feeService = new FeeService(afmClient, paymentMethodService);

        it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto serviceResponse = feeService
                .computeFee(Mono.just(paymentOptionDtoRequest), null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundleOptions().size());
    }
}
