package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@ValueObjects
@EqualsAndHashCode
public class PaymentInstrumentCategoryID implements Serializable {
    private UUID category;

    public PaymentInstrumentCategoryID(@NonNull UUID category) {

        this.category = Objects.requireNonNull(category);
    }

    public @NonNull UUID value() {
        return category;
    }
}
