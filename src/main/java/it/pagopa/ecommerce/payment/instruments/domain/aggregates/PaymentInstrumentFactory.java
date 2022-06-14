package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import static it.pagopa.ecommerce.payment.instruments.exception.PaymentInstrumentAlreadyInUseException.paymentInstrumentAlreadyInUse;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                                                        PaymentInstrumentStatus paymentInstrumentEnabled,
                                                        PaymentInstrumentType paymentInstrumentType) {

        return paymentInstrumentRepository.findByPaymentInstrumentName(paymentInstrumentName.value()).hasElements()
                .map(hasPaymentInstrument -> {
                    if (!hasPaymentInstrument) {
                        return new PaymentInstrument(paymentInstrumentID, paymentInstrumentName,
                                paymentInstrumentDescription,
                                paymentInstrumentEnabled, paymentInstrumentType);
                    } else {
                        throw paymentInstrumentAlreadyInUse(paymentInstrumentName);
                    }
                });
    }

}
