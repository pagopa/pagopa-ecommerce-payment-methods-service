package it.pagopa.ecommerce.payment.methods.exception;

/**
 * Exception raised when no bundle is found for searched payment method
 */
public class NoBundleFoundException extends RuntimeException {

    private final String paymentMethodId;
    private final long amount;

    private final String touchPoint;

    /**
     * Constructor
     *
     * @param paymentMethodId searched payment method id
     * @param amount          amount for which calculate fees
     * @param touchPoint      touchPoint used to filter payment methods
     */
    public NoBundleFoundException(
            String paymentMethodId,
            long amount,
            String touchPoint
    ) {
        super(
                "No bundle found for payment method with id: [%s] and transaction amount: [%s] for touch point: [%s]"
                        .formatted(paymentMethodId, amount, touchPoint)
        );
        this.paymentMethodId = paymentMethodId;
        this.amount = amount;
        this.touchPoint = touchPoint;
    }
}
