package it.pagopa.ecommerce.payment.instruments.exception;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryName;

public class CategoryAlreadyInUseException extends RuntimeException {

    private CategoryAlreadyInUseException(PaymentInstrumentCategoryName name) {
        super("PaymentInstrumentCategory: '" + name.value() + "' is already in use");
    }

    public static CategoryAlreadyInUseException categoryAlreadyInUseException(
            PaymentInstrumentCategoryName name) {
        return new CategoryAlreadyInUseException(name);
    }

}
