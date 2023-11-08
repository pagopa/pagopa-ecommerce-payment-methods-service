package it.pagopa.ecommerce.payment.methods.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

public record UniqueIdDocument(@NonNull @Id String id) {

}
