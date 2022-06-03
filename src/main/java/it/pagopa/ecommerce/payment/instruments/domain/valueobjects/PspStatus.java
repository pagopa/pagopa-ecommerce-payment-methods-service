package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PspStatus extends PaymentInstrumentStatus{
    public PspStatus(PaymentInstrumentStatusEnum status) {
        super(status);
    }
}