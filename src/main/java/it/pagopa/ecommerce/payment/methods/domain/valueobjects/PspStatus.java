package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PspStatus extends PaymentMethodStatus {
    public PspStatus(PaymentMethodStatusEnum status) {
        super(status);
    }
}