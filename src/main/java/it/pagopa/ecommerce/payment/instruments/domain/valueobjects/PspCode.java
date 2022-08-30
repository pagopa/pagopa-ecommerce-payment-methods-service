package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspCode implements Serializable {

    private final String id;

    public PspCode(@NonNull String id) {

        this.id = Objects.requireNonNull(id);
    }

    public @NonNull String value() {

        return id;
    }
}
