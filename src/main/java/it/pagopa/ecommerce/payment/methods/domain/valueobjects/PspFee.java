package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspFee implements Serializable {

    private final BigInteger fee;

    public PspFee(@NonNull BigInteger fee) {

        this.fee = Objects.requireNonNull(fee);
    }

    public @NonNull BigInteger value() {
        return fee;
    }
}
