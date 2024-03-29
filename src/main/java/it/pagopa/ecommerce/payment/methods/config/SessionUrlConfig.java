package it.pagopa.ecommerce.payment.methods.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "session-url")
public record SessionUrlConfig(
        URI basePath,
        String outcomeSuffix,
        String cancelSuffix,

        String notificationUrl
) {
}
