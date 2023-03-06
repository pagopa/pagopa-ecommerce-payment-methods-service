package it.pagopa.ecommerce.payment.methods.service;

import it.pagopa.ecommerce.payment.methods.application.FeeService;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentOptionDto;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class FeeServiceTests {

    @Mock
    private AfmClient afmClient;

    private FeeService feeService;

    @BeforeEach
    public void init() {
        feeService = new FeeService(afmClient);
    }

    @Test
    void shouldRetrieveFee() {
        PaymentOptionDto paymentOptionDtoRequest = TestUtil.getPaymentOptionRequest();
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();

        Mockito.when(afmClient.getFees(TestUtil.getPaymentOptionRequestClient(), null))
                .thenReturn(Mono.just(gecResponse));

        it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto serviceResponse = feeService
                .computeFee(Mono.just(paymentOptionDtoRequest), null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundleOptions().size());
    }
}
