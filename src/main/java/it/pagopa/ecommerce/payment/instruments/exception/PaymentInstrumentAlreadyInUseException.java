package it.pagopa.ecommerce.payment.instruments.exception;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentName;

public class PaymentInstrumentAlreadyInUseException extends RuntimeException {

    private PaymentInstrumentAlreadyInUseException(PaymentInstrumentName paymentInstrumentName) {
        super("PaymentInstrumentName '" + paymentInstrumentName.value() + "' is already in use");
    }

    public static PaymentInstrumentAlreadyInUseException paymentInstrumentAlreadyInUse(
            PaymentInstrumentName paymentInstrumentName) {
        return new PaymentInstrumentAlreadyInUseException(paymentInstrumentName);
    }

}
