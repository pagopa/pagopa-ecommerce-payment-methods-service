package it.pagopa.pspmatcher.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

@ValueObjects
public class PspDescription implements Serializable {

    private final String description;

    public PspDescription(@NonNull String description) {

        this.description = Objects.requireNonNull(description);
    }

    public @NonNull String value() {
        return description;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
            PspDescription that = (PspDescription) o;
        return description.equals(that.description);
    }

    @Override
    public int hashCode() {

        return description.hashCode();
    }
}
