package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspAmount;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspBrokerName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspBusinessName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspChannelCode;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspCode;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspFee;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspLanguage;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspPaymentMethodType;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspStatus;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.ecommerce.payment.methods.exception.PspAlreadyInUseException.pspAlreadyInUseException;

@Component
@AggregateFactory(PaymentMethod.class)
public class PspFactory {

    @Autowired
    private PspRepository pspRepository;

    @AggregateFactory(Psp.class)
    public Mono<Psp> newPsp(
                            PspCode pspCode,
                            PspPaymentMethodType pspPaymentMethodType,
                            PspStatus pspStatus,
                            PspBusinessName pspBusinessName,
                            PspBrokerName pspBrokerName,
                            PspDescription pspDescription,
                            PspLanguage pspLanguage,
                            PspAmount pspMinAmount,
                            PspAmount pspMaxAmount,
                            PspChannelCode pspChannelCode,
                            PspFee pspFixedCost
    ) {

        return pspRepository.findByPspDocumentKey(
                pspCode.value(),
                pspPaymentMethodType.value(),
                pspChannelCode.value()
        ).hasElements()
                .map(hasPsp -> {
                    if (Boolean.FALSE.equals(hasPsp)) {
                        return new Psp(
                                pspCode,
                                pspPaymentMethodType,
                                pspStatus,
                                pspBusinessName,
                                pspBrokerName,
                                pspDescription,
                                pspLanguage,
                                pspMinAmount,
                                pspMaxAmount,
                                pspChannelCode,
                                pspFixedCost
                        );
                    } else {
                        throw pspAlreadyInUseException(pspCode);
                    }
                });
    }
}
