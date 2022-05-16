package it.pagopa.pspmatcher.domain.aggregates;

import java.util.ArrayList;
import java.util.List;

import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentDescription;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentEnabled;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentID;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentName;
import it.pagopa.pspmatcher.domain.valueobjects.Psp;
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
    private PaymentInstrumentEnabled paymentInstrumentEnabled;

    @AggregateID
    public PaymentInstrumentID paymentInstrumentID() {
        return this.paymentInstrumentID;
    }

    public PaymentInstrument(PaymentInstrumentID paymentInstrumentID, PaymentInstrumentName paymentInstrumentName,
            PaymentInstrumentDescription paymentInstrumentDescription,
            PaymentInstrumentEnabled paymentInstrumentEnabled) {
        this.paymentInstrumentID = paymentInstrumentID;
        this.paymentInstrumentName = paymentInstrumentName;
        this.paymentInstrumentDescription = paymentInstrumentDescription;
        this.paymentInstrumentEnabled = paymentInstrumentEnabled;
        this.psp = new ArrayList<>();
    }

    public void enablePaymentInstrument(PaymentInstrumentEnabled paymentInstrumentEnabled) {

        this.paymentInstrumentEnabled = paymentInstrumentEnabled;
    }
}
