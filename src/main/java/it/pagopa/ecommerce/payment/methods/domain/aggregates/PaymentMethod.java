package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodAsset;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodID;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodRange;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodStatus;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodType;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
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
    private final List<PaymentMethodRange> paymentMethodRanges;
    private final PaymentMethodType paymentMethodTypeCode;
    private final PaymentMethodAsset paymentMethodAsset;
    private PaymentMethodStatus paymentMethodStatus;

    public PaymentMethod(
            PaymentMethodID paymentMethodID,
            PaymentMethodName paymentMethodName,
            PaymentMethodDescription paymentMethodDescription,
            PaymentMethodStatus paymentMethodStatus,
            PaymentMethodType paymentMethodTypeCode,
            List<PaymentMethodRange> paymentMethodRanges,
            PaymentMethodAsset paymentMethodAsset
    ) {
        this.paymentMethodID = paymentMethodID;
        this.paymentMethodName = paymentMethodName;
        this.paymentMethodDescription = paymentMethodDescription;
        this.paymentMethodStatus = paymentMethodStatus;
        this.paymentMethodTypeCode = paymentMethodTypeCode;
        this.paymentMethodRanges = paymentMethodRanges;
        this.paymentMethodAsset = paymentMethodAsset;
    }

    @AggregateID
    public PaymentMethodID paymentMethodID() {
        return this.paymentMethodID;
    }

    public void setPaymentMethodStatus(PaymentMethodStatusEnum paymentMethodStatus) {

        this.paymentMethodStatus = new PaymentMethodStatus(paymentMethodStatus);
    }
}
