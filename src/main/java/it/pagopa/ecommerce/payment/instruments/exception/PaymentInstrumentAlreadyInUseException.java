package it.pagopa.ecommerce.payment.instruments.exception;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentMethodName;

public class PaymentInstrumentAlreadyInUseException extends RuntimeException {

    private PaymentInstrumentAlreadyInUseException(PaymentMethodName paymentMethodName) {
        super("PaymentInstrumentName '" + paymentMethodName.value() + "' is already in use");
    }

    public static PaymentInstrumentAlreadyInUseException paymentInstrumentAlreadyInUse(
            PaymentMethodName paymentMethodName) {
        return new PaymentInstrumentAlreadyInUseException(paymentMethodName);
    }

}
