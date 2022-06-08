package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@ValueObjects
@EqualsAndHashCode
@Getter
public class Psp implements Serializable {

    private final PspCode pspCode;
    private final PspDescription pspDescription;
    private final PspFee pspFee;

    public Psp(@NonNull PspCode pspCode, @NonNull PspDescription pspDescription, @NonNull PspFee pspFee) {

        this.pspCode = Objects.requireNonNull(pspCode);
        this.pspDescription = Objects.requireNonNull(pspDescription);
        this.pspFee = Objects.requireNonNull(pspFee);
    }
}
