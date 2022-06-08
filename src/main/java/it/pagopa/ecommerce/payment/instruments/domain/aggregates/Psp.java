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
    private final PspCode pspCode;
    private final PaymentInstrumentID paymentInstrumentID;
    private PspStatus pspStatus;
    private final PspBusinessName pspBusinessName;
    private final PspBrokerName pspBrokerName;
    private final PspDescription pspDescription;
    private final List<Language> pspLangagues;
    private final List<String> pspRanges;

    private final PspPaymentInstrumentType pspPaymentInstrumentType;

    @AggregateID
    public PaymentInstrumentID paymentInstrumentID() {
        return this.paymentInstrumentID;
    }

    public Psp(PspCode pspCode, PaymentInstrumentID paymentInstrumentID, PspStatus pspStatus,
               PspBusinessName pspBusinessName, PspBrokerName pspBrokerName,
               PspDescription pspDescription, PspPaymentInstrumentType pspPaymentInstrumentType) {
        this.pspCode = pspCode;
        this.paymentInstrumentID = paymentInstrumentID;
        this.pspStatus = pspStatus;
        this.pspBusinessName = pspBusinessName;
        this.pspBrokerName = pspBrokerName;
        this.pspDescription = pspDescription;
        this.pspLangagues = new ArrayList();
        this.pspRanges = new ArrayList();
        this.pspPaymentInstrumentType = pspPaymentInstrumentType;
    }

    public void enablePsp(PspStatus pspStatus) {
        this.pspStatus = pspStatus;
    }
}
