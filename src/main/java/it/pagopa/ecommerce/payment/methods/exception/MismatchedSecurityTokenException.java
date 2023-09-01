package it.pagopa.ecommerce.payment.methods.exception;

public class MismatchedSecurityTokenException extends RuntimeException {
    public MismatchedSecurityTokenException(
            String sessionId,
            String transactionId
    ) {
        super(
                "Mismatched security token for session with session id: %s and transactionId: %s"
                        .formatted(sessionId, transactionId)
        );
    }
}
