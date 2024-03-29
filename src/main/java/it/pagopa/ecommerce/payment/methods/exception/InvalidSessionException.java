package it.pagopa.ecommerce.payment.methods.exception;

public class InvalidSessionException extends RuntimeException {
    public InvalidSessionException(String sessionId) {
        super("Null transaction id for session with order id: %s".formatted(sessionId));
    }
}
