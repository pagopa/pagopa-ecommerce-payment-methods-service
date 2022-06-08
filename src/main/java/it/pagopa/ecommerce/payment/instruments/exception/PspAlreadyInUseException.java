package it.pagopa.ecommerce.payment.instruments.exception;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PspCode;

public class PspAlreadyInUseException extends RuntimeException {

    private PspAlreadyInUseException(PspCode pspCode) {
        super("PSP '" + pspCode.value() + "' is already in use");
    }

    public static PspAlreadyInUseException pspAlreadyInUseException(
            PspCode pspCode) {
        return new PspAlreadyInUseException(pspCode);
    }

}
