package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.math.BigInteger;

public interface PspRepository extends ReactiveCrudRepository<PspDocument, String> {
    Flux<PspDocument> findByPspDocumentKey(String pspCode, String pspPaymentTypeCode, String pspChannelCode);
    @Query("{ 'pspMinAmount' : { $lt: ?0}, 'pspMaxAmount' : { $gt: ?0} }")
    Flux<PspDocument> findPspMatchAmount(BigInteger amount);

    @Query("{ '_id.pspPaymentTypeCode' : ?0 }")
    Flux<PspDocument> findPspMatchType(String type);

    @Query("{ '_id.pspLanguageCode' : ?0 }")
    Flux<PspDocument> findPspMatchLang(String language);

    @Query("{ 'pspMinAmount' : { $lt: ?0}, 'pspMaxAmount' : { $gt: ?0}, '_id.pspLanguageCode' : ?1 }")
    Flux<PspDocument> findPspMatchAmountLang(BigInteger amount, String languageCode);

    @Query("{ 'pspMinAmount' : { $lt: ?0}, 'pspMaxAmount' : { $gt: ?0}, '_id.pspPaymentTypeCode' : ?1 }")
    Flux<PspDocument> findPspMatchAmountType(BigInteger amount, String paymentTypeCode);

    @Query("{ '_id.pspPaymentTypeCode' : ?0 , '_id.pspLanguageCode' : ?1 }")
    Flux<PspDocument> findPspMatchTypeLang(String paymentTypeCode, String lang);

    @Query("{ 'pspMinAmount' : { $lt: ?0 }, 'pspMaxAmount' : { $gt: ?0 }, '_id.pspPaymentTypeCode' : ?1, '_id.pspLanguageCode' : ?2 }")
    Flux<PspDocument> findPspMatchAmountTypeLang(BigInteger amount, String pspPaymentTypeCode, String pspLanguageCode);

}