package it.pagopa.ecommerce.payment.methods.exception;

import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocumentKey;

public class PspNotFoundException extends RuntimeException {
    public PspNotFoundException(PspDocumentKey pspDocumentKey) {
        super("PSP not found with parameters: code=%s, paymentTypeCode=%s, channel=%s, language=%s".formatted(
                pspDocumentKey.getPspCode(),
                pspDocumentKey.getPspPaymentTypeCode(),
                pspDocumentKey.getPspChannelCode(),
                pspDocumentKey.getPspLanguageCode()
        ));
    }
}
