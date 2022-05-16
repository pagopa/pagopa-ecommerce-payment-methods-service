package it.pagopa.pspmatcher.exception;

//TODO improve
public class PaymentInstrumentStoreException extends RuntimeException {

    private PaymentInstrumentStoreException() {
        super("PaymentInstrument store error");
    }

    public static PaymentInstrumentStoreException paymentInstrumentStoreError() {
        return new PaymentInstrumentStoreException();
    }

}
