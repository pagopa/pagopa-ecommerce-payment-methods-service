package it.pagopa.ecommerce.payment.methods.exception;

public class PyamentMethodNotFoundException extends RuntimeException {

    public PyamentMethodNotFoundException(String paymentMethodId) {
        super("paymentMethodId with id '" + paymentMethodId + "' not found");
    }
}
