package it.pagopa.ecommerce.payment.instruments.exception;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryID;

public class CategoryNotFoundException extends RuntimeException {

    private CategoryNotFoundException(PaymentInstrumentCategoryID id) {
        super("PaymentInstrumentCategory: '" + id.value() + "' not found");
    }

    public static CategoryNotFoundException categoryNotFoundException(
            PaymentInstrumentCategoryID id) {
        return new CategoryNotFoundException(id);
    }

}
