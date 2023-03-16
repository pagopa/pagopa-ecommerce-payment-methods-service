package it.pagopa.ecommerce.payment.methods.exception;

public class PaymentMethodNotFoundException extends RuntimeException {

    public PaymentMethodNotFoundException(String paymentMethodId) {
        super("paymentMethodId with id '" + paymentMethodId + "' not found");
    }
}
