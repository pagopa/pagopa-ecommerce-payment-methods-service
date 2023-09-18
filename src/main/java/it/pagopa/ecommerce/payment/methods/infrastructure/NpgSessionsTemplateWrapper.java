package it.pagopa.ecommerce.payment.methods.infrastructure;

import it.pagopa.ecommerce.commons.redis.templatewrappers.RedisTemplateWrapper;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public class NpgSessionsTemplateWrapper extends RedisTemplateWrapper<NpgSessionDocument> {
    /**
     * Primary constructor
     *
     * @param redisTemplate inner redis template
     * @param keyspace      keyspace associated to this wrapper
     * @param ttl           time to live for keys
     */
    public NpgSessionsTemplateWrapper(
            RedisTemplate<String, NpgSessionDocument> redisTemplate,
            String keyspace,
            Duration ttl
    ) {
        super(redisTemplate, keyspace, ttl);
    }

    @Override
    protected String getKeyFromEntity(NpgSessionDocument value) {
        return value.orderId();
    }
}
