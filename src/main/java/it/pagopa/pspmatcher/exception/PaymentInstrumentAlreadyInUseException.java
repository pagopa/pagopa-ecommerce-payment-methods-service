package it.pagopa.pspmatcher.exception;

import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentName;

public class PaymentInstrumentAlreadyInUseException extends RuntimeException {

    private PaymentInstrumentAlreadyInUseException(PaymentInstrumentName paymentInstrumentName) {
        super("PaymentInstrumentName '" + paymentInstrumentName.value() + "' is already in use");
    }

    public static PaymentInstrumentAlreadyInUseException paymentInstrumentAlreadyInUse(
            PaymentInstrumentName paymentInstrumentName) {
        return new PaymentInstrumentAlreadyInUseException(paymentInstrumentName);
    }

}
