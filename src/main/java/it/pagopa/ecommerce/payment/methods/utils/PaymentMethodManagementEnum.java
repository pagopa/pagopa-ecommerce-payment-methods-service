package it.pagopa.ecommerce.payment.methods.utils;

public enum PaymentMethodManagementEnum {

    ONBOARDABLE("ONBOARDABLE"),
    NOT_ONBOARDABLE("NOT_ONBOARDABLE"),
    REDIRECT("REDIRECT");

    private final String code;

    PaymentMethodManagementEnum(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
