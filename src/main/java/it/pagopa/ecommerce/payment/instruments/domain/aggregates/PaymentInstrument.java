package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import java.util.ArrayList;
import java.util.List;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentDescription;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentStatus;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentID;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentName;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.Psp;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Aggregate
public class PaymentInstrument {

    private final PaymentInstrumentID paymentInstrumentID;
    private final PaymentInstrumentName paymentInstrumentName;
    private final PaymentInstrumentDescription paymentInstrumentDescription;
    private final List<Psp> psp;
    private PaymentInstrumentStatus paymentInstrumentStatus;

    @AggregateID
    public PaymentInstrumentID paymentInstrumentID() {
        return this.paymentInstrumentID;
    }

    public PaymentInstrument(PaymentInstrumentID paymentInstrumentID, PaymentInstrumentName paymentInstrumentName,
            PaymentInstrumentDescription paymentInstrumentDescription,
            PaymentInstrumentStatus paymentInstrumentStatus) {
        this.paymentInstrumentID = paymentInstrumentID;
        this.paymentInstrumentName = paymentInstrumentName;
        this.paymentInstrumentDescription = paymentInstrumentDescription;
        this.paymentInstrumentStatus = paymentInstrumentStatus;
        this.psp = new ArrayList<>();
    }

    public void enablePaymentInstrument(PaymentInstrumentStatus paymentInstrumentStatus) {

        this.paymentInstrumentStatus = paymentInstrumentStatus;
    }
}
