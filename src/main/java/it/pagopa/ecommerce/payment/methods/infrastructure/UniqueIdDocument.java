package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.lang.NonNull;

@RedisHash(value = "keys")
public record UniqueIdDocument(@NonNull @Id String id) {
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
    @PersistenceConstructor
    public UniqueIdDocument {
        // Do nothing
    }
}
