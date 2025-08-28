package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@RedisHash(value = "keys", timeToLive = 10 * 60)
public record NpgSessionDocument(
        @NonNull @Id String orderId,
        @NonNull String correlationId,
        @NonNull String sessionId,
        @NonNull String securityToken,
        @Nullable CardDataDocument cardData,
        @Nullable String transactionId

) {
    /*
     * @formatter:off
     *
     * Warning java:S6207 - Redundant constructors/methods should be avoided in records
     * Suppressed because this constructor is just to add the `@PersistenceConstructor` annotation
     * and is currently the canonical way to add annotations to record constructors
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S6207")
    @PersistenceCreator
    public NpgSessionDocument {
        // Do nothing
    }
}
