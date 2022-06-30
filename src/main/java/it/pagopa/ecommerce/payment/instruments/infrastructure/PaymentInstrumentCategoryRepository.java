package it.pagopa.ecommerce.payment.instruments.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PaymentInstrumentCategoryRepository  extends ReactiveCrudRepository<PaymentInstrumentCategoryDocument, String>{
    Mono<PaymentInstrumentCategoryDocument> findBypaymentInstrumentCategoryName(String name);
}
