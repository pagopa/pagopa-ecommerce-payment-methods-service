package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentMethodRepository extends ReactiveCrudRepository<PaymentMethodDocument, String> {
    Mono<PaymentMethodDocument> findByPaymentMethodNameOrPaymentMethodTypeCode(
                                                                               String paymentMethodName,
                                                                               String paymentMethodTypeCode
    );

    Mono<PaymentMethodDocument> findByPaymentMethodTypeCode(String paymentMethodTypeCode);

    Flux<PaymentMethodDocument> findByPaymentMethodStatus(String paymentMethodStatus);

    Flux<PaymentMethodDocument> findByClientId(String clientId);

    Mono<PaymentMethodDocument> findByPaymentMethodIDAndClientId(
                                                                 String paymentMethodID,
                                                                 String clientId
    );

    Mono<PaymentMethodDocument> findByPaymentMethodNameAndPaymentMethodTypeCodeAndClientId(
                                                                                           String paymentMethodName,
                                                                                           String paymentMethodTypeCode,
                                                                                           String clientId
    );

}
