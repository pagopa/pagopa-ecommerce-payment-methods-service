package it.pagopa.ecommerce.payment.methods.exception;

public class OrderIdNotFoundException extends RuntimeException {

    public OrderIdNotFoundException(String orderId) {
        super("Session with order id '%s' not found".formatted(orderId));
    }
}
