package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

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
