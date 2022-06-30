package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Aggregate
public class PaymentInstrumentCategory {
    private final PaymentInstrumentCategoryID paymentInstrumentCategoryID;

    private final List<PaymentInstrumentType> paymentInstrumentTypes;

    private final PaymentInstrumentCategoryName paymentInstrumentCategoryName;

    public PaymentInstrumentCategory(PaymentInstrumentCategoryID paymentInstrumentCategoryID,
                                     PaymentInstrumentCategoryName paymentInstrumentCategoryName){
        this.paymentInstrumentCategoryID = paymentInstrumentCategoryID;
        this.paymentInstrumentCategoryName = paymentInstrumentCategoryName;
        this.paymentInstrumentTypes = new ArrayList<>();
    }

    @AggregateID
    public PaymentInstrumentCategoryID paymentInstrumentID() {
        return this.paymentInstrumentCategoryID;
    }

}
