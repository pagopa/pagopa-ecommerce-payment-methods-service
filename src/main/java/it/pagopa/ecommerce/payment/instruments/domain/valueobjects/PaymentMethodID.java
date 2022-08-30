package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@ValueObjects
@EqualsAndHashCode
public class PaymentMethodID implements Serializable {

    private final UUID uuid;

    public PaymentMethodID(@NonNull UUID uuid) {

        this.uuid = Objects.requireNonNull(uuid);
    }

    public @NonNull UUID value() {
        return uuid;
    }
}
