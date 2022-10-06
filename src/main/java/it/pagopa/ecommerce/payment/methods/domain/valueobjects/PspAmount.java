package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspAmount implements Serializable {

    private final Double amount;

    public PspAmount(@NonNull Double amount) {

        this.amount = Objects.requireNonNull(amount);
    }

    public @NonNull Double value() {
        return amount;
    }
}
