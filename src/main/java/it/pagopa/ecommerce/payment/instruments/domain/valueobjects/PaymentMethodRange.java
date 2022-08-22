package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import org.springframework.lang.NonNull;

import javax.validation.ValidationException;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;

public class PaymentMethodRange implements Serializable {

    private final int min;
    private final int max;

    public PaymentMethodRange(@NonNull @PositiveOrZero int min, @NonNull @PositiveOrZero int max) {
        if (min > max){
            throw new ValidationException("Invalid range");
        }

        this.min = min;
        this.max = max;
    }

    public @NonNull int min() {
        return min;
    }
    public @NonNull int max() {
        return max;
    }
}