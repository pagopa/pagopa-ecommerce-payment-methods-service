package it.pagopa.ecommerce.payment.instruments.exception;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentMethodName;

public class PaymentMethodAlreadyInUseException extends RuntimeException {

    private PaymentMethodAlreadyInUseException(PaymentMethodName paymentMethodName) {
        super("PaymentInstrumentName '" + paymentMethodName.value() + "' is already in use");
    }

    public static PaymentMethodAlreadyInUseException paymentInstrumentAlreadyInUse(
            PaymentMethodName paymentMethodName) {
        return new PaymentMethodAlreadyInUseException(paymentMethodName);
    }

}
