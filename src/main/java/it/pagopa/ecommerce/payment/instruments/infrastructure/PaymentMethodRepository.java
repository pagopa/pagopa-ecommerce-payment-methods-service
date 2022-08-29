package it.pagopa.ecommerce.payment.instruments.infrastructure;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentMethodRepository extends ReactiveCrudRepository<PaymentMethodDocument, String> {
    Flux<PaymentMethodDocument> findByPaymentMethodName(String paymentInstrumentName);
    Mono<PaymentMethodDocument> findByPaymentMethodID(String paymentMethodID);
    Mono<PaymentMethodDocument> findByPaymentMethodNameOrPaymentMethodTypeCode(String paymentMethodID,
                                                                               String paymentMethodTypeCode);
    Mono<PaymentMethodDocument> findByPaymentMethodTypeCode(String paymentMethodTypeCode);

}
