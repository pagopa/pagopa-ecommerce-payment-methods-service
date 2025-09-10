package it.pagopa.ecommerce.payment.methods.config;

import it.pagopa.ecommerce.commons.redis.reactivetemplatewrappers.ReactiveUniqueIdTemplateWrapper;
import it.pagopa.ecommerce.commons.utils.ReactiveUniqueIdUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UniqueIdConfiguration {
    @Bean
    public ReactiveUniqueIdUtils uniqueIdUtils(ReactiveUniqueIdTemplateWrapper uniqueIdTemplateWrapper) {
        return new ReactiveUniqueIdUtils(uniqueIdTemplateWrapper);
    }
}
