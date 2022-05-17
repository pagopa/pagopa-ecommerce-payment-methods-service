package it.pagopa.pspmatcher.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@ValueObjects
@EqualsAndHashCode
@Getter
public class Psp implements Serializable {

    private final PspID pspId;
    private final PspDescription pspDescription;
    private final PspFee pspFee;

    public Psp(@NonNull PspID pspId, @NonNull PspDescription pspDescription, @NonNull PspFee pspFee) {

        this.pspId = Objects.requireNonNull(pspId);
        this.pspDescription = Objects.requireNonNull(pspDescription);
        this.pspFee = Objects.requireNonNull(pspFee);
    }
}
