package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.lang.NonNull;

@RedisHash(value = "keys", timeToLive = 10 * 60)
public record NpgSessionDocument(
        @NonNull @Id String sessionId,
        @NonNull String securityToken
) {
    @PersistenceConstructor
    public NpgSessionDocument {
        // Do nothing
    }
}
