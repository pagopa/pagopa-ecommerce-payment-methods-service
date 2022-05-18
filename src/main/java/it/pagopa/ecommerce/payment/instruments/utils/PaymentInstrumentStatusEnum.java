package it.pagopa.ecommerce.payment.instruments.utils;

public enum PaymentInstrumentStatusEnum {

    ENABLED("ENABLED"),
    DISABLED("DISABLED"),
    INCOMING("INCOMING");

    private final String code;

    PaymentInstrumentStatusEnum(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
