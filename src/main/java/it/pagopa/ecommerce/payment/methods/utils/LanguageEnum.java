package it.pagopa.ecommerce.payment.methods.utils;

public enum LanguageEnum {

    IT("IT"),
    EN("EN"),
    FR("FR"),
    DE("DE"),
    SL("SL");
    private final String language;

    LanguageEnum(final String language) { this.language = language; }

    public String getLanguage() { return language; }
}
