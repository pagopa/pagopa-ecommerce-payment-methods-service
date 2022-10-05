package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PaymentMethodName implements Serializable {

    private final String name;

    public PaymentMethodName(@NonNull String name) {

        this.name = Objects.requireNonNull(name);
    }

    public @NonNull String value() {

        return name;
    }
}
