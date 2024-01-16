package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodManagementTypeDto;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PaymentMethodManagement {

    private final PaymentMethodManagementTypeDto managementMethodType;

    public PaymentMethodManagement(@NonNull PaymentMethodManagementTypeDto managementMethodType) {

        this.managementMethodType = Objects.requireNonNull(managementMethodType);
    }

    public @NonNull PaymentMethodManagementTypeDto value() {

        return managementMethodType;
    }

}
