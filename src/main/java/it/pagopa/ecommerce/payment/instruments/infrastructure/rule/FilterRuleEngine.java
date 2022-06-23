package it.pagopa.ecommerce.payment.instruments.infrastructure.rule;

import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.util.List;

@AllArgsConstructor
public class FilterRuleEngine {
    private List<IFilterRule> rules;
    @Autowired
    private PspRepository pspRepository;

    public Flux<PspDocument> applyFilter(Integer amount, String language, String paymentTypeCode){
        for(IFilterRule rule: rules){
            if(rule.shouldExecute(amount, language, paymentTypeCode)){
                return rule.execute(pspRepository, amount, language, paymentTypeCode);
            }
        }

        return Flux.empty();
    }
}
