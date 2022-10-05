package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

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
