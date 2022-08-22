package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PspStatus extends PaymentMethodStatus {
    public PspStatus(PaymentInstrumentStatusEnum status) {
        super(status);
    }
}