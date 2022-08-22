package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

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
