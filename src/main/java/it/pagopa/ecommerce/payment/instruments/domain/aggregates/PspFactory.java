package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.ecommerce.payment.instruments.exception.PspAlreadyInUseException.pspAlreadyInUseException;

@Component
@AggregateFactory(PaymentMethod.class)
public class PspFactory {

    @Autowired
    private PspRepository pspRepository;

    @AggregateFactory(Psp.class)
    public Mono<Psp> newPsp(PspCode pspCode, PspPaymentMethodType pspPaymentMethodType, PspStatus pspStatus,
                            PspBusinessName pspBusinessName, PspBrokerName pspBrokerName,
                            PspDescription pspDescription, PspLanguage pspLanguage,
                            PspAmount pspMinAmount, PspAmount pspMaxAmount,
                            PspChannelCode pspChannelCode, PspFee pspFixedCost) {

        return pspRepository.findByPspDocumentKey(
                        pspCode.value(),
                        pspPaymentMethodType.value(),
                        pspChannelCode.value()
                ).hasElements()
                .map(hasPsp -> {
                    if (Boolean.FALSE.equals(hasPsp)) {
                        return new Psp(pspCode, pspPaymentMethodType, pspStatus, pspBusinessName,
                                pspBrokerName, pspDescription, pspLanguage, pspMinAmount, pspMaxAmount,
                                pspChannelCode, pspFixedCost);
                    } else {
                        throw pspAlreadyInUseException(pspCode);
                    }
                });
    }
}
