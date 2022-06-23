package it.pagopa.ecommerce.payment.instruments.infrastructure.rule;

import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class TypeLangFilterRule implements IFilterRule {
    @Override
    public boolean shouldExecute(Integer amount, String language, String paymentTypeCode) {
        return checkQueryParam(paymentTypeCode) && checkQueryParam(language) && amount == null;
    }

    @Override
    public Flux<PspDocument> execute(PspRepository pspRepository, Integer amount, String language, String paymentTypeCode) {
        return pspRepository.findPspMatchTypeLang(paymentTypeCode, language);
    }
}
