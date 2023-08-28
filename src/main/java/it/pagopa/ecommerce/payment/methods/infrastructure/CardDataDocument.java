package it.pagopa.ecommerce.payment.methods.infrastructure;

import it.pagopa.ecommerce.commons.annotations.ValueObject;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

@ValueObject
public record CardDataDocument(
        @NonNull String bin,
        @NonNull String lastFourDigits,
        @NonNull String expiringDate,
        @NonNull String circuit

) {
    /**
     * Structure to identify the card data information.
     *
     * @param bin            card bin
     * @param lastFourDigits card last four digit
     * @param expiringDate   card expiring date
     * @param circuit        card circuit
     */
    @PersistenceConstructor
    public CardDataDocument {
        // Do nothing
    }
}
