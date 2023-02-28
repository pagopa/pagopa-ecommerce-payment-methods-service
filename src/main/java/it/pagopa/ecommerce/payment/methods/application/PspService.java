package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PspFactory;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.infrastructure.rule.FilterRuleEngine;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@ApplicationService
@Slf4j
public class PspService {

    @Autowired
    private PspFactory pspFactory;

    @Autowired
    private AfmClient afmClient;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private FilterRuleEngine filterRuleEngine;

    /*
    public void getPsp(String paymentMethodId, PaymentOptionDto paymentOptionDto){
       afmClient
                .getFees(paymentOptionDto)
                .map(bundleOptionDto ->
                    bundleOptionDto
                            .getBundleOptions()
                            .stream()
                            .map(transferDto ->
                                    Pair.of(transferDto.getIdPsp(), transferDto)
                            )
                ).map(
                        it -> it.collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())))
               );
    }
     */
}
