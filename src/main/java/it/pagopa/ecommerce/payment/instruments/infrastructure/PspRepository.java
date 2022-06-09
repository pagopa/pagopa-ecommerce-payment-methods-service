package it.pagopa.ecommerce.payment.instruments.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PspRepository extends ReactiveCrudRepository<PspDocument, String> {
    Flux<PspDocument> findByPspDocumentKey(String pspCode, String pspPaymentTypeCode, String pspChannelCode);
}