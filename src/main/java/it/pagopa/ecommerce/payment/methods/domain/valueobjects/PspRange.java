package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspRange implements Serializable {

    private final BigDecimal min;
    private final BigDecimal max;

    public PspRange(
            @NonNull BigDecimal min,
            @NonNull BigDecimal max
    ) {

        if (min.compareTo(max) != -1) {
            throw new IllegalArgumentException("Invalid range.");
        }

        this.min = Objects.requireNonNull(min);
        this.max = Objects.requireNonNull(max);
    }

    public @NonNull BigDecimal min() {
        return min;
    }

    public @NonNull BigDecimal max() {
        return max;
    }
}
