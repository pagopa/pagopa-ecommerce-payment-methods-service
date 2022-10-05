package it.pagopa.ecommerce.payment.methods.infrastructure.rule;

import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspRepository;
import reactor.core.publisher.Flux;

public interface IFilterRule {
    boolean shouldExecute(Integer amount, String language, String paymentTypeCode);
    Flux<PspDocument> execute(PspRepository pspRepository, Integer amount, String language, String paymentTypeCode);
    default boolean checkQueryParam(String param){
        return !(param == null || param.isBlank() || param.isEmpty());
    }

}
