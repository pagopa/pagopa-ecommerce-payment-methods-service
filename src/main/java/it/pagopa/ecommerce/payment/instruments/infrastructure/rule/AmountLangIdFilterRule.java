package it.pagopa.ecommerce.payment.instruments.infrastructure.rule;

import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class AmountLangIdFilterRule implements IFilterRule{
    @Override
    public boolean shouldExecute(String paymentInstrumentId, Integer amount, String language, String paymentTypeCode) {
        return checkQueryParam(paymentInstrumentId) && checkQueryParam(language)
                && amount != null && !checkQueryParam(paymentTypeCode);
    }

    @Override
    public Flux<PspDocument> execute(PspRepository pspRepository, String paymentInstrumentId, Integer amount, String language, String paymentTypeCode) {
        return pspRepository.findPspMatchAmountLangId(amount, language, paymentInstrumentId);
    }
}
