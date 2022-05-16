package it.pagopa.pspmatcher.domain.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.lang.NonNull;

@ValueObjects
public class PspFee implements Serializable {

    private final BigDecimal fee;

    public PspFee(@NonNull BigDecimal fee) {

        this.fee = Objects.requireNonNull(fee);
    }

    public @NonNull BigDecimal value() {
        return fee;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
            PspFee that = (PspFee) o;
        return fee.equals(that.fee);
    }

    @Override
    public int hashCode() {

        return fee.hashCode();
    }
}
