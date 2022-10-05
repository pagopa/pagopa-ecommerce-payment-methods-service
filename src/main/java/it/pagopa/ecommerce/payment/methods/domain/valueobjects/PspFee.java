package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspFee implements Serializable {

    private final Double fee;

    public PspFee(@NonNull Double fee) {

        this.fee = Objects.requireNonNull(fee);
    }

    public @NonNull Double value() {
        return fee;
    }
}
