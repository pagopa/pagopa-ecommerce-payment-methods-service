package it.pagopa.pspmatcher.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PaymentInstrumentID implements Serializable {

    private final UUID uuid;

    public PaymentInstrumentID(@NonNull UUID uuid) {

        this.uuid = Objects.requireNonNull(uuid);
    }

    public @NonNull UUID value() {
        return uuid;
    }
}
