package it.pagopa.ecommerce.payment.methods.client;

import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.generated.ecommerce.handler.v1.api.PaymentMethodsApi;
import it.pagopa.generated.ecommerce.handler.v1.dto.PaymentMethodResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentMethodsHandlerClient {

    private final PaymentMethodsApi paymentMethodsApi;

    public PaymentMethodsHandlerClient(
            @Qualifier("paymentMethodsHandlerWebClient") PaymentMethodsApi paymentMethodsApi
    ) {
        this.paymentMethodsApi = paymentMethodsApi;
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
        return paymentMethodsApi.getPaymentMethod(paymentMethodId, clientId)
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
     * @param clientId        the client ID to validate against
     * @return a Mono that completes with the response if the payment method exists,
     *         or errors with PaymentMethodNotFoundException
     */
    public Mono<PaymentMethodResponseDto> validatePaymentMethodExists(
                                                                      String paymentMethodId,
                                                                      String clientId
    ) {
        return getPaymentMethod(paymentMethodId, clientId);
    }
}
