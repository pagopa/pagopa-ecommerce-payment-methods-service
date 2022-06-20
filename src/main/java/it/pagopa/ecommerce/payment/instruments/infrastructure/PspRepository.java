package it.pagopa.ecommerce.payment.instruments.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PspRepository extends ReactiveCrudRepository<PspDocument, String> {
    Flux<PspDocument> findByPspDocumentKey(String pspCode, String pspPaymentTypeCode, String pspChannelCode);
    Flux<PspDocument> findByPspDocumentKeyPspLanguageCode(String lang);
    Flux<PspDocument> findByPspMinAmountLessThanEqualAndPspMaxAmountGreaterThanEqual(double min, double max);

    Flux<PspDocument> findByPspMinAmountLessThanEqualAndPspMaxAmountGreaterThanEqualAndPspDocumentKeyPspLanguageCode(
            double min, double max, String lang);
    
    Flux<PspDocument> findByPspMinAmountLessThanEqualAndPspMaxAmountGreaterThanEqualAndPspDocumentKeyPspLanguageCodeAndPspDocumentKeyPspPaymentTypeCode(
                double min, double max, String lang, String paymentTypeCode);   
 
    Flux<PspDocument> findByPspDocumentKeyPspLanguageCodeAndPspDocumentKeyPspPaymentTypeCode(String lang, String paymentTypeCode);
    Flux<PspDocument> findByPspMinAmountLessThanEqualAndPspMaxAmountGreaterThanEqualAndPspDocumentKeyPspPaymentTypeCode(double min, double max, String paymentTypeCode);                
}