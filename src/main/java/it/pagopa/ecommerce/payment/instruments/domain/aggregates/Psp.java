package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import java.util.ArrayList;
import java.util.List;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Aggregate
public class Psp {
    private final PspID pspID;
    private final PaymentInstrumentID paymentInstrumentID;
    private PspStatus pspStatus;
    private final PspBusinessName pspBusinessName;
    private final PspBrokerName pspBrokerName;
    private final PspDescription pspDescription;
    private final List<Language> pspLangagues;
    private final List<String> pspRanges;

    @AggregateID
    public PaymentInstrumentID paymentInstrumentID() {
        return this.paymentInstrumentID;
    }

    public Psp(PspID pspID, PaymentInstrumentID paymentInstrumentID, PspStatus pspStatus,
               PspBusinessName pspBusinessName, PspBrokerName pspBrokerName,
               PspDescription pspDescription) {
        this.pspID = pspID;
        this.paymentInstrumentID = paymentInstrumentID;
        this.pspStatus = pspStatus;
        this.pspBusinessName = pspBusinessName;
        this.pspBrokerName = pspBrokerName;
        this.pspDescription = pspDescription;
        this.pspLangagues = new ArrayList();
        this.pspRanges = new ArrayList();
    }

    public void enablePsp(PspStatus pspStatus) {
        this.pspStatus = pspStatus;
    }
}
