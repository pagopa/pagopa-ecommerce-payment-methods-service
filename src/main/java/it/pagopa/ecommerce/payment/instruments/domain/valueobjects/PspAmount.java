package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

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
