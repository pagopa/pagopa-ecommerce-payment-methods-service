package it.pagopa.ecommerce.payment.methods.exception;

public class SessionIdNotFoundException extends RuntimeException {

    public SessionIdNotFoundException(String sessionId) {
        super("Session with id '" + sessionId + "' not found");
    }
}
