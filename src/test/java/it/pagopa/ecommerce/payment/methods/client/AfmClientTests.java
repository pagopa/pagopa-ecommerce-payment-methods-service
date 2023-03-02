package it.pagopa.ecommerce.payment.methods.client;

import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.api.CalculatorApi;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class AfmClientTests {

    @Autowired
    @Qualifier("afmWebClient")
    private CalculatorApi afmClient;

    @Value("${afm.client.key}")
    private String afmKey;


    @Test
    void shouldRetrieveFee() {
        PaymentOptionDto paymentOptionDtoRequest = TestUtil.getPaymentOptionRequestClient();
        BundleOptionDto gecResponse = afmClient
                .getApiClient()
                .getWebClient()
                .post()
                .body(Mono.just(paymentOptionDtoRequest), it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto.class)
                .header("ocp-apim-subscription-key", afmKey)
                .retrieve()
                .bodyToMono(BundleOptionDto.class).block();

      assertNotNull(gecResponse);
    }
}
