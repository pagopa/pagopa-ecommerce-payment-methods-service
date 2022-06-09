package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import it.pagopa.ecommerce.payment.instruments.utils.LanguageEnum;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PspLanguage implements Serializable {
    private final LanguageEnum language;

    public PspLanguage(@NonNull LanguageEnum language) {

        this.language = Objects.requireNonNull(language);
    }

    public @NonNull LanguageEnum value() {

        return language;
    }
}
