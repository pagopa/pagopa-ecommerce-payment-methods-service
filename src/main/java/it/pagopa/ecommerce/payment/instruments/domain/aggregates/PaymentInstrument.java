package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Aggregate
public class PaymentInstrument {

    private final PaymentInstrumentID paymentInstrumentID;
    private final PaymentInstrumentName paymentInstrumentName;
    private final PaymentInstrumentDescription paymentInstrumentDescription;
    private final PaymentInstrumentCategoryID paymentInstrumentCategory;
    private PaymentInstrumentStatus paymentInstrumentStatus;

    @AggregateID
    public PaymentInstrumentID paymentInstrumentID() {
        return this.paymentInstrumentID;
    }

    public PaymentInstrument(PaymentInstrumentID paymentInstrumentID, PaymentInstrumentName paymentInstrumentName,
                             PaymentInstrumentDescription paymentInstrumentDescription,
                             PaymentInstrumentStatus paymentInstrumentStatus, PaymentInstrumentCategoryID paymentInstrumentCategory) {
        this.paymentInstrumentID = paymentInstrumentID;
        this.paymentInstrumentName = paymentInstrumentName;
        this.paymentInstrumentDescription = paymentInstrumentDescription;
        this.paymentInstrumentStatus = paymentInstrumentStatus;
        this.paymentInstrumentCategory = paymentInstrumentCategory;
    }

    public void enablePaymentInstrument(PaymentInstrumentStatus paymentInstrumentStatus) {

        this.paymentInstrumentStatus = paymentInstrumentStatus;
    }
}
