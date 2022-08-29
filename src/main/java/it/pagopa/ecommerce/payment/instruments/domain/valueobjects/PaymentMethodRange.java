package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import org.springframework.lang.NonNull;

import javax.validation.ValidationException;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;

public class PaymentMethodRange implements Serializable {

    private final long min;
    private final long max;

    public PaymentMethodRange(@NonNull @PositiveOrZero Long min, @NonNull @PositiveOrZero Long max) {
        if (min > max){
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