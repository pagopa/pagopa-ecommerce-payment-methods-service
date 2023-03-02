package it.pagopa.ecommerce.payment.methods.controller;

import it.pagopa.ecommerce.payment.methods.application.FeeService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentOptionDto;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@WebFluxTest(FeeController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
public class FeeControllerTests {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private AfmClient afmClient;

    @MockBean
    private FeeService feeService;

    @Test
    void shouldGetFees() {
        PaymentOptionDto requestBody = TestUtil.getPaymentOptionRequest();
        it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto gecResponse = TestUtil
                .getBundleOptionDtoClientResponse();
        Mockito.when(feeService.computeFee(any(), any()))
                .thenReturn(Mono.just(gecResponse));

        BundleOptionDto expectedResult = TestUtil.getBundleOptionDtoResponseFromClientResponse(gecResponse);

        webClient
                .post()
                .uri("/fee/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(BundleOptionDto.class)
                .isEqualTo(expectedResult);
    }

}
