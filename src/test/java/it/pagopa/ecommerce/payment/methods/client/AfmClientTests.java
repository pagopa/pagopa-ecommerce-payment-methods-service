package it.pagopa.ecommerce.payment.methods.client;

import static it.pagopa.ecommerce.payment.methods.client.AfmClient.HEADER_APIM_KEY;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.ecommerce.payment.methods.config.WebClientsConfig;
import it.pagopa.ecommerce.payment.methods.exception.AfmResponseException;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AfmClientTests {
    private static MockWebServer mockWebServer;

    private final WebClientsConfig clientsConfig = new WebClientsConfig(16777216);

    private AfmClient afmClient;

    @BeforeEach
    public void init() {
        final var calculatorApi = clientsConfig
                .afmWebClient("http://localhost:9001/v1/fees", 5000, 5000);
        final var calculatorApiV2 = clientsConfig
                .afmWebClientV2("http://localhost:9001/v2/fees", 5000, 5000);
        afmClient = new AfmClient(calculatorApi, calculatorApiV2, "xxx");
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockWebServer.close();
    }

    @BeforeAll
    public static void tearUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(9001);
        System.out.println("Start mock server");
    }

    @ParameterizedTest
    @ValueSource(
            booleans = {
                    false,
                    true
            }
    )
    void shouldRetrieveFeeFromGEC(Boolean allCcp)
            throws JsonProcessingException, InterruptedException {
        final var gecResponse = TestUtil.getBundleOptionDtoClientResponse();
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(new ObjectMapper().writeValueAsString(gecResponse))
        );

        StepVerifier
                .create(afmClient.getFees(TestUtil.getPaymentOptionRequestClient(), 10, allCcp))
                .expectNext(gecResponse)
                .verifyComplete();

        assertThat(mockWebServer.takeRequest().getHeader(HEADER_APIM_KEY)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("it.pagopa.ecommerce.payment.methods.client.AfmClientTests#negativeStatusCode")
    void shouldReturnResponseStatusException(HttpStatus httpStatus) throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(httpStatus.value()).setBody("{\"error\": \"wrong\"}"));

        StepVerifier
                .create(afmClient.getFees(TestUtil.getPaymentOptionRequestClient(), 10, false))
                .expectError(AfmResponseException.class)
                .verify();

        assertThat(mockWebServer.takeRequest().getHeader(HEADER_APIM_KEY)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("it.pagopa.ecommerce.payment.methods.client.AfmClientTests#negativeStatusCode")
    void shouldReturnAfmResponseExceptionWithNegativeStatusCodeAndEmptyBody(HttpStatus errorCode)
            throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(errorCode.value()));

        StepVerifier
                .create(
                        afmClient.getFees(
                                TestUtil.getPaymentOptionRequestClient(),
                                10,
                                false
                        )
                )
                .expectError(AfmResponseException.class)
                .verify();

        assertThat(mockWebServer.takeRequest().getHeader(HEADER_APIM_KEY)).isNotNull();
    }

    @Nested
    class V2 {

        @ParameterizedTest
        @ValueSource(
                booleans = {
                        false,
                        true
                }
        )
        void shouldRetrieveFeeFromGEC(Boolean allCcp)
                throws JsonProcessingException, InterruptedException {
            final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();
            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody(new ObjectMapper().writeValueAsString(gecResponse))
            );

            StepVerifier
                    .create(
                            afmClient.getFeesForNotices(
                                    TestUtil.V2.getPaymentMultiNoticeOptionRequestClient(),
                                    10,
                                    allCcp
                            )
                    )
                    .expectNext(gecResponse)
                    .verifyComplete();

            assertThat(mockWebServer.takeRequest().getHeader(HEADER_APIM_KEY)).isNotNull();
        }

        @ParameterizedTest
        @MethodSource("it.pagopa.ecommerce.payment.methods.client.AfmClientTests#negativeStatusCode")
        void shouldReturnAfmResponseException(HttpStatus errorCode) throws InterruptedException {
            mockWebServer
                    .enqueue(new MockResponse().setResponseCode(errorCode.value()).setBody("{\"error\": \"wrong\"}"));

            StepVerifier
                    .create(
                            afmClient.getFeesForNotices(
                                    TestUtil.V2.getPaymentMultiNoticeOptionRequestClient(),
                                    10,
                                    false
                            )
                    )
                    .expectError(AfmResponseException.class)
                    .verify();
            assertThat(mockWebServer.takeRequest().getHeader("ocp-apim-subscription-key")).isNotNull();
        }

        @ParameterizedTest
        @MethodSource("it.pagopa.ecommerce.payment.methods.client.AfmClientTests#negativeStatusCode")
        void shouldReturnAfmResponseExceptionWithNegativeStatusCodeAndEmptyBody(HttpStatus errorCode)
                throws InterruptedException {
            mockWebServer.enqueue(new MockResponse().setResponseCode(errorCode.value()));

            StepVerifier
                    .create(
                            afmClient.getFeesForNotices(
                                    TestUtil.V2.getPaymentMultiNoticeOptionRequestClient(),
                                    10,
                                    false
                            )
                    )
                    .expectError(AfmResponseException.class)
                    .verify();

            assertThat(mockWebServer.takeRequest().getHeader(HEADER_APIM_KEY)).isNotNull();
        }
    }

    public static Stream<Arguments> negativeStatusCode() {
        return Arrays.stream(HttpStatus.values()).filter(HttpStatus::isError)
                .map(Arguments::of);
    }
}
