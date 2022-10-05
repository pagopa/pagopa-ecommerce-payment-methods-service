package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspBrokerName implements Serializable {

    private final String brokerName;

    public PspBrokerName(@NonNull String brokerName) {

        this.brokerName = Objects.requireNonNull(brokerName);
    }

    public @NonNull String value() {
        return brokerName;
    }
}
