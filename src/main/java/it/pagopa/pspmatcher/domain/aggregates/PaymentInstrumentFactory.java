package it.pagopa.pspmatcher.domain.aggregates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentDescription;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentEnabled;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentID;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentName;
import it.pagopa.pspmatcher.infrastructure.PaymentInstrumentRepository;

import static it.pagopa.pspmatcher.exception.PaymentInstrumentAlreadyInUseException.paymentInstrumentAlreadyInUse;

import reactor.core.publisher.Mono;

@Component
@AggregateFactory(PaymentInstrument.class)
public class PaymentInstrumentFactory {

    @Autowired
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @AggregateFactory(PaymentInstrument.class)
    public Mono<PaymentInstrument> newPaymentInstrument(PaymentInstrumentID paymentInstrumentID,
            PaymentInstrumentName paymentInstrumentName,
            PaymentInstrumentDescription paymentInstrumentDescription,
            PaymentInstrumentEnabled paymentInstrumentEnabled) {

        return paymentInstrumentRepository.findByPaymentInstrumentName(paymentInstrumentName.value()).hasElements()
                .map(hasPaymentInstrument -> {
                    if (!hasPaymentInstrument) {
                        return new PaymentInstrument(paymentInstrumentID, paymentInstrumentName,
                                paymentInstrumentDescription,
                                paymentInstrumentEnabled);
                    } else {
                        throw paymentInstrumentAlreadyInUse(paymentInstrumentName);
                    }
                });
    }

}
