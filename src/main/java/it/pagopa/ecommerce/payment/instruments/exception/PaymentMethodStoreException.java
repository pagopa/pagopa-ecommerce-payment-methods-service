package it.pagopa.ecommerce.payment.instruments.exception;

//TODO improve
public class PaymentMethodStoreException extends RuntimeException {

    private PaymentMethodStoreException() {
        super("PaymentInstrument store error");
    }

    public static PaymentMethodStoreException paymentInstrumentStoreError() {
        return new PaymentMethodStoreException();
    }

}
