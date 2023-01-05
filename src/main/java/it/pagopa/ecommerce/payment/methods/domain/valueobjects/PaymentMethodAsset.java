package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PaymentMethodAsset implements Serializable {

    private final String asset;

    public PaymentMethodAsset(@NonNull @NotBlank String asset) {

        this.asset = Objects.requireNonNull(asset);
    }

    public @NonNull String value() {
        return asset;
    }
}
