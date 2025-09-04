package it.pagopa.ecommerce.payment.methods.config;

import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.ReactiveUniqueIdTemplateWrapper;
import it.pagopa.ecommerce.commons.repositories.UniqueIdDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Bean
    public NpgSessionsTemplateWrapper npgSessionsTemplateWrapper(
                                                                 ReactiveRedisConnectionFactory redisConnectionFactory,
                                                                 @Value("${npg.sessionsTTL}") int sessionsTtl
    ) {
        Jackson2JsonRedisSerializer<NpgSessionDocument> jacksonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                NpgSessionDocument.class
        );
        RedisSerializationContext<String, NpgSessionDocument> serializationContext = RedisSerializationContext
                .<String, NpgSessionDocument>newSerializationContext(new StringRedisSerializer())
                .value(jacksonRedisSerializer).build();

        ReactiveRedisTemplate<String, NpgSessionDocument> reactiveRedisTemplate = new ReactiveRedisTemplate<>(
                redisConnectionFactory,
                serializationContext
        );
        return new NpgSessionsTemplateWrapper(
                reactiveRedisTemplate,
                "npg",
                Duration.ofSeconds(sessionsTtl)
        );
    }

    @Bean
    public ReactiveUniqueIdTemplateWrapper uniqueIdTemplateWrapper(
                                                                   ReactiveRedisConnectionFactory redisConnectionFactory
    ) {
        Jackson2JsonRedisSerializer<UniqueIdDocument> jacksonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                UniqueIdDocument.class
        );
        RedisSerializationContext<String, UniqueIdDocument> serializationContext = RedisSerializationContext
                .<String, UniqueIdDocument>newSerializationContext(new StringRedisSerializer())
                .value(jacksonRedisSerializer).build();

        ReactiveRedisTemplate<String, UniqueIdDocument> reactiveRedisTemplate = new ReactiveRedisTemplate<>(
                redisConnectionFactory,
                serializationContext
        );
        return new ReactiveUniqueIdTemplateWrapper(
                reactiveRedisTemplate,
                "uniqueId",
                Duration.ofSeconds(60)
        );
    }
}
