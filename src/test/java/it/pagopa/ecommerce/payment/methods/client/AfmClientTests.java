package it.pagopa.ecommerce.payment.methods.client;

import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.ApiClient;
import it.pagopa.generated.ecommerce.gec.v1.api.CalculatorApi;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AfmClientTests {
    @Mock
    private ApiClient apiClient;
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    @Qualifier("afmWebClient")
    private CalculatorApi calculatorApi;

    @Mock
    @Qualifier("afmWebClientV2")
    private it.pagopa.generated.ecommerce.gec.v2.api.CalculatorApi calculatorApiV2;

    private AfmClient afmClient;

    @BeforeEach
    public void init() {
        afmClient = new AfmClient(calculatorApi, calculatorApiV2, "xxx");
    }

    @Test
    void shouldRetrieveFeeFromGECAllCCPFalse() {
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();
        Mockito.when(calculatorApi.getApiClient()).thenReturn(apiClient);
        Mockito.when(apiClient.getWebClient()).thenReturn(webClient);
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.body(any(Publisher.class), any(Class.class))).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(BundleOptionDto.class)).thenReturn(Mono.just(gecResponse));

        StepVerifier
                .create(afmClient.getFees(TestUtil.getPaymentOptionRequestClient(), 10, false))
                .expectNext(gecResponse)
                .verifyComplete();
    }

    @Test
    void shouldRetrieveFeeFromGECAllCCPTrue() {
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();
        Mockito.when(calculatorApi.getApiClient()).thenReturn(apiClient);
        Mockito.when(apiClient.getWebClient()).thenReturn(webClient);
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.body(any(Publisher.class), any(Class.class))).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(BundleOptionDto.class)).thenReturn(Mono.just(gecResponse));

        StepVerifier
                .create(afmClient.getFees(TestUtil.getPaymentOptionRequestClient(), 10, true))
                .expectNext(gecResponse)
                .verifyComplete();
    }

    @Test
    void shouldReturnResponseStatusException() {
        Mockito.when(calculatorApi.getApiClient()).thenReturn(apiClient);
        Mockito.when(apiClient.getWebClient()).thenReturn(webClient);
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.body(any(Publisher.class), any(Class.class))).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(BundleOptionDto.class))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)));

        StepVerifier
                .create(afmClient.getFees(TestUtil.getPaymentOptionRequestClient(), 10, false))
                .expectError(ResponseStatusException.class);
    }

    @Test
    void shouldReturnException() {
        Mockito.when(calculatorApi.getApiClient()).thenReturn(apiClient);
        Mockito.when(apiClient.getWebClient()).thenReturn(webClient);
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        Mockito.when(requestBodySpec.body(any(Publisher.class), any(Class.class))).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(BundleOptionDto.class)).thenReturn(Mono.error(new Exception()));

        StepVerifier
                .create(afmClient.getFees(TestUtil.getPaymentOptionRequestClient(), 10, false))
                .expectError(Exception.class);
    }

}
