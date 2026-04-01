package it.pagopa.ecommerce.payment.methods.client;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.ecommerce.payment.methods.config.WebClientsConfig;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.generated.ecommerce.handler.v1.dto.PaymentMethodResponseDto;
import java.io.IOException;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PaymentMethodsHandlerClientTest {

    private static MockWebServer mockWebServer;

    private final WebClientsConfig clientsConfig = new WebClientsConfig(16777216);

    private PaymentMethodsHandlerClient client;

    private static final String PAYMENT_METHOD_ID = "e7058cac-5e1a-4002-8994-5bab31e9f385";
    private static final String API_KEY = "test-api-key";

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(9002);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.close();
    }

    @BeforeEach
    void init() {
        final var handlerApi = clientsConfig
                .paymentMethodsHandlerWebClient("http://localhost:9002", 5000, 5000);
        client = new PaymentMethodsHandlerClient(handlerApi, API_KEY);
    }

    @Test
    void shouldReturnPaymentMethodWhenFound() throws JsonProcessingException, InterruptedException {
        PaymentMethodResponseDto responseDto = new PaymentMethodResponseDto()
                .name(Map.of("it", "CARDS"))
                .status(PaymentMethodResponseDto.StatusEnum.ENABLED);

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(new ObjectMapper().writeValueAsString(responseDto))
        );

        StepVerifier.create(client.getPaymentMethod(PAYMENT_METHOD_ID, "CHECKOUT"))
                .expectNextMatches(r -> r.getName().get("it").equals("CARDS"))
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("x-api-key")).isEqualTo(API_KEY);
        assertThat(request.getHeader("x-client-id")).isEqualTo("CHECKOUT");
        assertThat(request.getPath()).isEqualTo("/payment-methods/" + PAYMENT_METHOD_ID);
    }

    @Test
    void shouldThrowPaymentMethodNotFoundExceptionOn404() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404).setBody("{}"));

        StepVerifier.create(client.getPaymentMethod(PAYMENT_METHOD_ID, "CHECKOUT"))
                .expectError(PaymentMethodNotFoundException.class)
                .verify();

        mockWebServer.takeRequest();
    }

    @Test
    void shouldPropagateWebClientExceptionOnOtherErrors() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("{\"error\": \"internal\"}"));

        StepVerifier.create(client.getPaymentMethod(PAYMENT_METHOD_ID, "CHECKOUT"))
                .expectError(WebClientResponseException.class)
                .verify();

        mockWebServer.takeRequest();
    }

    @Test
    void shouldCallGetPaymentMethodWithCheckoutClientIdWhenValidating()
            throws JsonProcessingException, InterruptedException {
        PaymentMethodResponseDto responseDto = new PaymentMethodResponseDto()
                .name(Map.of("it", "CARDS"));

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(new ObjectMapper().writeValueAsString(responseDto))
        );

        StepVerifier.create(client.validatePaymentMethodExists(PAYMENT_METHOD_ID))
                .expectNextMatches(r -> r.getName().get("it").equals("CARDS"))
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("x-client-id")).isEqualTo("CHECKOUT");
    }
}
