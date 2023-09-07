package it.pagopa.ecommerce.payment.methods.exception;

public class SessionAlreadyAssociatedToTransaction extends RuntimeException {
    public SessionAlreadyAssociatedToTransaction(
            String sessionId,
            String existingTransactionid,
            String requestedTransactionId
    ) {
        super(
                "Requested session association to transaction id %s for session with id %s: already associated to transaction id %s"
                        .formatted(requestedTransactionId, sessionId, existingTransactionid)
        );
    }
}
