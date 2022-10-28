package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspAmount implements Serializable {

    private final BigInteger amount;

    public PspAmount(@NonNull BigInteger amount) {

        this.amount = Objects.requireNonNull(amount);
    }

    public @NonNull BigInteger value() {
        return amount;
    }
}
