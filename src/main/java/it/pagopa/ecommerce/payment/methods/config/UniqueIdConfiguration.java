package it.pagopa.ecommerce.payment.methods.config;

import it.pagopa.ecommerce.commons.redis.templatewrappers.UniqueIdTemplateWrapper;
import it.pagopa.ecommerce.commons.utils.UniqueIdUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UniqueIdConfiguration {
    @Bean
    public UniqueIdUtils uniqueIdUtils(UniqueIdTemplateWrapper uniqueIdTemplateWrapper) {
        return new UniqueIdUtils(uniqueIdTemplateWrapper);
    }
}
