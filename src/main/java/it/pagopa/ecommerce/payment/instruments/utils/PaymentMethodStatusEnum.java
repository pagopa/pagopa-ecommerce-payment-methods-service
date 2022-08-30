package it.pagopa.ecommerce.payment.instruments.utils;

public enum PaymentMethodStatusEnum {

    ENABLED("ENABLED"),
    DISABLED("DISABLED"),
    INCOMING("INCOMING");

    private final String code;

    PaymentMethodStatusEnum(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
