package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class PspBusinessName implements Serializable {

    private final String businessName;

    public PspBusinessName(@NonNull String businessName) {

        this.businessName = Objects.requireNonNull(businessName);
    }

    public @NonNull String value() {
        return businessName;
    }
}
