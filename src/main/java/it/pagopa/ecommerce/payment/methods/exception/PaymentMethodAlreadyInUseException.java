package it.pagopa.ecommerce.payment.methods.exception;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;

public class PaymentMethodAlreadyInUseException extends RuntimeException {

    private PaymentMethodAlreadyInUseException(PaymentMethodName paymentMethodName) {
        super("PaymentmethodName '" + paymentMethodName.value() + "' is already in use");
    }

    public static PaymentMethodAlreadyInUseException paymentmethodAlreadyInUse(
            PaymentMethodName paymentMethodName) {
        return new PaymentMethodAlreadyInUseException(paymentMethodName);
    }

}
