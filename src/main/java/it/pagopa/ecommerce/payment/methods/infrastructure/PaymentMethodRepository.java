package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentMethodRepository extends ReactiveCrudRepository<PaymentMethodDocument, String> {
    Flux<PaymentMethodDocument> findByPaymentMethodName(String paymentmethodName);

    Mono<PaymentMethodDocument> findByPaymentMethodNameOrPaymentMethodTypeCode(String paymentMethodID,
                                                                               String paymentMethodTypeCode);
    Mono<PaymentMethodDocument> findByPaymentMethodTypeCode(String paymentMethodTypeCode);

}
