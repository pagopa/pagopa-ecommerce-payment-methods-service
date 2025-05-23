package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import org.springframework.lang.NonNull;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;

public class PaymentMethodRange implements Serializable {

    private final long min;
    private final long max;

    public PaymentMethodRange(
            @PositiveOrZero long min,
            @PositiveOrZero long max
    ) {
        if (min > max) {
            throw new ValidationException("Invalid range");
        }

        this.min = min;
        this.max = max;
    }

    public @NonNull long min() {
        return min;
    }

    public @NonNull long max() {
        return max;
    }
}
