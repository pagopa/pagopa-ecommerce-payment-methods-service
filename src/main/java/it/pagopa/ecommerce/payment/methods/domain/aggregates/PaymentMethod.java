package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodAsset;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodID;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodRange;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodStatus;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodType;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
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
    private final NpgClient.PaymentMethod npgPaymentMethod;
    private final PaymentMethodRequestDto.ClientIdEnum clientIdEnum;

    private PaymentMethodStatus paymentMethodStatus;

    private final boolean isRedirect;

    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because PaymentMethod is a simple data container with no logic.
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    public PaymentMethod(
            PaymentMethodID paymentMethodID,
            PaymentMethodName paymentMethodName,
            PaymentMethodDescription paymentMethodDescription,
            PaymentMethodStatus paymentMethodStatus,
            PaymentMethodType paymentMethodTypeCode,
            List<PaymentMethodRange> paymentMethodRanges,
            PaymentMethodAsset paymentMethodAsset,
            NpgClient.PaymentMethod npgPaymentMethod,
            PaymentMethodRequestDto.ClientIdEnum clientIdEnum,
            boolean isRedirect
    ) {
        this.paymentMethodID = paymentMethodID;
        this.paymentMethodName = paymentMethodName;
        this.paymentMethodDescription = paymentMethodDescription;
        this.paymentMethodStatus = paymentMethodStatus;
        this.paymentMethodTypeCode = paymentMethodTypeCode;
        this.paymentMethodRanges = paymentMethodRanges;
        this.paymentMethodAsset = paymentMethodAsset;
        this.npgPaymentMethod = npgPaymentMethod;
        this.clientIdEnum = clientIdEnum;
        this.isRedirect = isRedirect;
    }

    @AggregateID
    public PaymentMethodID paymentMethodID() {
        return this.paymentMethodID;
    }

    public void setPaymentMethodStatus(PaymentMethodStatusEnum paymentMethodStatus) {

        this.paymentMethodStatus = new PaymentMethodStatus(paymentMethodStatus);
    }
}
