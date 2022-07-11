package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Aggregate
public class PaymentInstrument {

    private final PaymentInstrumentID paymentInstrumentID;
    private final PaymentInstrumentName paymentInstrumentName;
    private final PaymentInstrumentDescription paymentInstrumentDescription;
    private final PaymentInstrumentCategoryID paymentInstrumentCategoryID;
    private PaymentInstrumentStatus paymentInstrumentStatus;

    private final PaymentInstrumentCategoryName paymentInstrumentCategoryName;

    private final List<PaymentInstrumentType> paymentInstrumentCategoryTypes;

    private final PaymentInstrumentType paymentInstrumentTypeCode;

    @AggregateID
    public PaymentInstrumentID paymentInstrumentID() {
        return this.paymentInstrumentID;
    }

    public PaymentInstrument(PaymentInstrumentID paymentInstrumentID, PaymentInstrumentName paymentInstrumentName,
                             PaymentInstrumentDescription paymentInstrumentDescription,
                             PaymentInstrumentStatus paymentInstrumentStatus,
                             PaymentInstrumentCategoryID paymentInstrumentCategory,
                             PaymentInstrumentCategoryName paymentInstrumentCategoryName,
                             List<PaymentInstrumentType> paymentInstrumentCategoryTypes,
                             PaymentInstrumentType paymentInstrumentTypeCode) {
        this.paymentInstrumentID = paymentInstrumentID;
        this.paymentInstrumentName = paymentInstrumentName;
        this.paymentInstrumentDescription = paymentInstrumentDescription;
        this.paymentInstrumentStatus = paymentInstrumentStatus;
        this.paymentInstrumentCategoryID = paymentInstrumentCategory;
        this.paymentInstrumentCategoryName = paymentInstrumentCategoryName;
        this.paymentInstrumentCategoryTypes  = paymentInstrumentCategoryTypes;
        this.paymentInstrumentTypeCode = paymentInstrumentTypeCode;
    }

    public void enablePaymentInstrument(PaymentInstrumentStatus paymentInstrumentStatus) {

        this.paymentInstrumentStatus = paymentInstrumentStatus;
    }
}
