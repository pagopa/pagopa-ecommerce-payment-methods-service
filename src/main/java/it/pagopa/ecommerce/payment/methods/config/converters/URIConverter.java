package it.pagopa.ecommerce.payment.methods.config.converters;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@ConfigurationPropertiesBinding
public class URIConverter implements Converter<String, URI> {
    @Override
    public URI convert(@NonNull String uri) {
        return URI.create(uri);
    }
}
