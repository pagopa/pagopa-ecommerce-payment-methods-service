package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PaymentMethodDescription implements Serializable {

    private final String description;

    public PaymentMethodDescription(@NonNull String description) {

        this.description = Objects.requireNonNull(description);
    }

    public @NonNull String value() {
        return description;
    }
}
