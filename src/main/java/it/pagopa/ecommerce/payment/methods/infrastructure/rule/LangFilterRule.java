package it.pagopa.ecommerce.payment.methods.infrastructure.rule;

import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class LangFilterRule implements IFilterRule {
    @Override
    public boolean shouldExecute(
                                 Integer amount,
                                 String language,
                                 String paymentTypeCode
    ) {
        return !checkQueryParam(paymentTypeCode) && checkQueryParam(language) && amount == null;
    }

    @Override
    public Flux<PspDocument> execute(
                                     PspRepository pspRepository,
                                     Integer amount,
                                     String language,
                                     String paymentTypeCode
    ) {
        return pspRepository.findPspMatchLang(language);
    }

}
