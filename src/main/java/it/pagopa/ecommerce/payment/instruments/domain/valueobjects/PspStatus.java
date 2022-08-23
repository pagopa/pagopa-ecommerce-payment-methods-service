package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import it.pagopa.ecommerce.payment.instruments.utils.PaymentMethodStatusEnum;
import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PspStatus extends PaymentMethodStatus {
    public PspStatus(PaymentMethodStatusEnum status) {
        super(status);
    }
}