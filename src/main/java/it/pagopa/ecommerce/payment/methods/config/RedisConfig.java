package it.pagopa.ecommerce.payment.methods.config;

import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Bean
    public NpgSessionsTemplateWrapper npgSessionsTemplateWrapper(
                                                                 RedisConnectionFactory redisConnectionFactory,
                                                                 @Value("${npg.sessionsTTL}") int sessionsTtl
    ) {
        RedisTemplate<String, NpgSessionDocument> redisTemplate = new RedisTemplate<>();
        Jackson2JsonRedisSerializer<NpgSessionDocument> jacksonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                NpgSessionDocument.class
        );

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jacksonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return new NpgSessionsTemplateWrapper(
                redisTemplate,
                "npg",
                Duration.ofSeconds(sessionsTtl)
        );
    }
}
