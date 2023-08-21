package it.pagopa.ecommerce.payment.methods.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "preauthorization-url")
public record PreauthorizationUrlConfig(
        URI basePath,
        String outcomeSuffix,
        String cancelSuffix
) {
}
