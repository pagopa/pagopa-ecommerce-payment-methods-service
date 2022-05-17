package it.pagopa.ecommerce.payment.instruments.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface PaymentInstrumentRepository extends ReactiveCrudRepository<PaymentInstrumentDocument, String> {
    Flux<PaymentInstrumentDocument> findByPaymentInstrumentName(String paymentInstrumentName);
}
