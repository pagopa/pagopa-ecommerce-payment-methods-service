package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import static it.pagopa.ecommerce.payment.instruments.exception.PaymentInstrumentAlreadyInUseException.paymentInstrumentAlreadyInUse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentDescription;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentStatus;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentID;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentName;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentRepository;
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
            PaymentInstrumentStatus paymentInstrumentEnabled) {

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
