package it.pagopa.pspmatcher.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

@ValueObjects
public class PspID implements Serializable {

    private final String id;

    public PspID(@NonNull String id) {

        this.id = Objects.requireNonNull(id);
    }

    public @NonNull String value() {

        return id;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
            PspID that = (PspID) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {

        return id.hashCode();
    }
}
