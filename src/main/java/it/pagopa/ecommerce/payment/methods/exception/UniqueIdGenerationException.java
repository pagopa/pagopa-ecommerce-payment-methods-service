package it.pagopa.ecommerce.payment.methods.exception;

public class UniqueIdGenerationException extends RuntimeException {
    public UniqueIdGenerationException() {
        super(
                "Error when generating unique id"
        );
    }
}
