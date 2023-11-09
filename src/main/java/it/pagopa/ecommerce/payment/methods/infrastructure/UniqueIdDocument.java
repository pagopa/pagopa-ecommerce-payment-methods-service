package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;

public record UniqueIdDocument(
        @NonNull @Id String id,
        String creationDate
) {

    public UniqueIdDocument(String id) {
        this(id, OffsetDateTime.now().toString());
    }
}
