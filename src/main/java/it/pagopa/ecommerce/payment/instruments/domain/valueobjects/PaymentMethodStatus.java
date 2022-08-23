package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import it.pagopa.ecommerce.payment.instruments.utils.PaymentMethodStatusEnum;
import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PaymentMethodStatus implements Serializable {

    private final PaymentMethodStatusEnum status;

    public PaymentMethodStatus(@NonNull PaymentMethodStatusEnum status) {

        this.status = Objects.requireNonNull(status);
    }

    public @NonNull PaymentMethodStatusEnum value() {

        return status;
    }
}
