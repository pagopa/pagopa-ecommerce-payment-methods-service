package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PaymentInstrumentCategoryName implements Serializable {
    private String name;

    public PaymentInstrumentCategoryName(@NonNull String name) {

        this.name = Objects.requireNonNull(name);
    }

    public @NonNull String value() {
        return name;
    }
}
