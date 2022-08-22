package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Aggregate
public class PaymentMethod {

    private final PaymentMethodID paymentMethodID;
    private final PaymentMethodName paymentMethodName;
    private final PaymentMethodDescription paymentMethodDescription;
    private PaymentMethodStatus paymentMethodStatus;

    private final List<PaymentMethodRange> paymentMethodRanges;
    private final PaymentMethodType paymentMethodTypeCode;

    @AggregateID
    public PaymentMethodID paymentMethodID() {
        return this.paymentMethodID;
    }

    public PaymentMethod(PaymentMethodID paymentMethodID, PaymentMethodName paymentMethodName,
                         PaymentMethodDescription paymentMethodDescription,
                         PaymentMethodStatus paymentMethodStatus,
                         PaymentMethodType paymentMethodTypeCode,
                         List<PaymentMethodRange> paymentMethodRanges) {
        this.paymentMethodID = paymentMethodID;
        this.paymentMethodName = paymentMethodName;
        this.paymentMethodDescription = paymentMethodDescription;
        this.paymentMethodStatus = paymentMethodStatus;
        this.paymentMethodTypeCode = paymentMethodTypeCode;
        this.paymentMethodRanges = paymentMethodRanges;
    }

    public void enablePaymentInstrument(PaymentMethodStatus paymentMethodStatus) {

        this.paymentMethodStatus = paymentMethodStatus;
    }
}
