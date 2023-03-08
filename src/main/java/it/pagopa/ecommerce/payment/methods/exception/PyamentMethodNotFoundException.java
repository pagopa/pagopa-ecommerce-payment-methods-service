package it.pagopa.ecommerce.payment.methods.exception;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;

public class PyamentMethodNotFoundException extends RuntimeException {

    public PyamentMethodNotFoundException(String paymentMethodId) {
        super("paymentMethodId with id '" + paymentMethodId + "' not found");
    }
}
