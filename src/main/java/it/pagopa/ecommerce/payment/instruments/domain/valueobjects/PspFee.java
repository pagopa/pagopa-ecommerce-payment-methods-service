package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PspFee implements Serializable {

    private final BigDecimal fee;

    public PspFee(@NonNull BigDecimal fee) {

        this.fee = Objects.requireNonNull(fee);
    }

    public @NonNull BigDecimal value() {
        return fee;
    }
}
