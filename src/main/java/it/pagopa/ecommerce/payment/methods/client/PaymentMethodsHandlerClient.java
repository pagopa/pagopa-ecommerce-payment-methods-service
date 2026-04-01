package it.pagopa.ecommerce.payment.methods.client;

import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.generated.ecommerce.handler.v1.api.PaymentMethodsApi;
import it.pagopa.generated.ecommerce.handler.v1.dto.PaymentMethodResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentMethodsHandlerClient {

    private static final String X_API_KEY_HEADER = "x-api-key";

    private final PaymentMethodsApi paymentMethodsApi;
    private final String apiKey;

    public PaymentMethodsHandlerClient(
            @Qualifier("paymentMethodsHandlerWebClient") PaymentMethodsApi paymentMethodsApi,
            @Value("${paymentMethodsHandler.apiKey}") String apiKey
    ) {
        this.paymentMethodsApi = paymentMethodsApi;
        this.apiKey = apiKey;
    }

    /**
     * Retrieve a payment method by ID from the payment-methods-handler service.
     *
     * @param paymentMethodId the payment method ID
     * @param clientId        the client ID (e.g. IO, CHECKOUT, CHECKOUT_CART)
     * @return a Mono containing the payment method response
     */
    public Mono<PaymentMethodResponseDto> getPaymentMethod(
                                                           String paymentMethodId,
                                                           String clientId
    ) {
        return paymentMethodsApi.getApiClient().getWebClient()
                .get()
                .uri("/payment-methods/{id}", paymentMethodId)
                .header("x-client-id", clientId)
                .header(X_API_KEY_HEADER, apiKey)
                .retrieve()
                .bodyToMono(PaymentMethodResponseDto.class)
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> {
                            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                                return Mono.error(
                                        new PaymentMethodNotFoundException(paymentMethodId)
                                );
                            }
                            log.error(
                                    "Error calling payment-methods-handler for id {}: {}",
                                    paymentMethodId,
                                    e.getResponseBodyAsString()
                            );
                            return Mono.error(e);
                        }
                );
    }

    /**
     * Validate that a payment method exists by calling the handler service.
     *
     * @param paymentMethodId the payment method ID
     * @return a Mono that completes with the response if the payment method exists,
     *         or errors with PaymentMethodNotFoundException
     */
    public Mono<PaymentMethodResponseDto> validatePaymentMethodExists(String paymentMethodId) {
        return getPaymentMethod(paymentMethodId, "CHECKOUT");
    }
}
