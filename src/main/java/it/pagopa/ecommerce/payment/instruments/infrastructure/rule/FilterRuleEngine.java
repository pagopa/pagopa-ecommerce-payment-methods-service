package it.pagopa.ecommerce.payment.instruments.infrastructure.rule;

import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
@Component
public class FilterRuleEngine {
    @Autowired
    private List<IFilterRule> filterRules;
    @Autowired
    private PspRepository pspRepository;


    public Flux<PspDocument> applyFilter(Integer amount, String language, String paymentTypeCode){
        for(IFilterRule rule: filterRules){
            if(rule.shouldExecute(amount, language, paymentTypeCode)){
                return rule.execute(pspRepository, amount, language, paymentTypeCode);
            }
        }

        return Flux.empty();
    }
}
