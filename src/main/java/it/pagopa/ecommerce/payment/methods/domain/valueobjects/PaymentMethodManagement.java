package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodManagementEnum;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.util.Objects;

@ValueObjects
@EqualsAndHashCode
public class PaymentMethodManagement {

    private final PaymentMethodManagementEnum status;

    public PaymentMethodManagement(@NonNull PaymentMethodManagementEnum status) {

        this.status = Objects.requireNonNull(status);
    }

    public @NonNull PaymentMethodManagementEnum value() {

        return status;
    }

}
