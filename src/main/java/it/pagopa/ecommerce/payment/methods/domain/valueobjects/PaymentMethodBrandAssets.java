package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import java.util.Map;
import java.util.Optional;

@ValueObjects
public record PaymentMethodBrandAssets(Optional<Map<String, String>> brandAssets) {

}
