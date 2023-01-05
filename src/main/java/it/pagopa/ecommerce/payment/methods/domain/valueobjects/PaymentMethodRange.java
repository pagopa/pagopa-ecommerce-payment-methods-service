package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import org.springframework.lang.NonNull;

import javax.validation.ValidationException;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.math.BigInteger;

public class PaymentMethodRange implements Serializable {

    private final BigInteger min;
    private final BigInteger max;

    public PaymentMethodRange(
            @NonNull @PositiveOrZero BigInteger min,
            @NonNull @PositiveOrZero BigInteger max
    ) {
        if (min.compareTo(max) > 0) {
            throw new ValidationException("Invalid range");
        }

        this.min = min;
        this.max = max;
    }

    public @NonNull BigInteger min() {
        return min;
    }

    public @NonNull BigInteger max() {
        return max;
    }
}
