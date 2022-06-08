package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static it.pagopa.ecommerce.payment.instruments.exception.PspAlreadyInUseException.pspAlreadyInUseException;

@Component
@AggregateFactory(PaymentInstrument.class)
public class PspFactory {

    @Autowired
    private PspRepository pspRepository;

    @AggregateFactory(Psp.class)
    public Mono<Psp> newPsp(PspCode pspCode, PaymentInstrumentID paymentInstrumentID, PspStatus pspStatus,
                            PspBusinessName pspBusinessName, PspBrokerName pspBrokerName,
                            PspDescription pspDescription, PspPaymentInstrumentType pspPaymentInstrumentType) {

        return pspRepository.findByPspCode(pspCode.value()).hasElements()
                .map(hasPsp -> {
                    if (!hasPsp) {
                       return new Psp(pspCode, paymentInstrumentID, pspStatus, pspBusinessName,
                                pspBrokerName, pspDescription, pspPaymentInstrumentType);
                    } else {
                        throw pspAlreadyInUseException(pspCode);
                    }
                });
    }
}
