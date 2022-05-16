package it.pagopa.pspmatcher.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PaymentInstrumentEnabled implements Serializable {

    private final Boolean enabled;

    public PaymentInstrumentEnabled(@NonNull Boolean enabled) {

        this.enabled = Objects.requireNonNull(enabled);
    }

    public @NonNull Boolean value() {

        return enabled;
    }
}
