package it.pagopa.ecommerce.payment.instruments.infrastructure.rule;

import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import reactor.core.publisher.Flux;

public interface IFilterRule {
    boolean shouldExecute(String paymentInstrumentId, Integer amount, String language, String paymentTypeCode);
    Flux<PspDocument> execute(PspRepository pspRepository, String paymentInstrumentId, Integer amount, String language, String paymentTypeCode);
    default boolean checkQueryParam(String param){
        return !(param == null || param.isBlank() || param.isEmpty());
    }

}
