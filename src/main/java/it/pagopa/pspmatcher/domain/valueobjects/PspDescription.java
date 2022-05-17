package it.pagopa.pspmatcher.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PspDescription implements Serializable {

    private final String description;

    public PspDescription(@NonNull String description) {

        this.description = Objects.requireNonNull(description);
    }

    public @NonNull String value() {
        return description;
    }
}
