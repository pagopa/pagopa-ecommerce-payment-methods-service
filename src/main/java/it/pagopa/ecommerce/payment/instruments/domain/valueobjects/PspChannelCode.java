package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class PspChannelCode implements Serializable {

    private final String pspChannelCode;

    public PspChannelCode(@NonNull String pspChannelCode) {

        this.pspChannelCode = Objects.requireNonNull(pspChannelCode);
    }

    public @NonNull String value() {
        return pspChannelCode;
    }
}
