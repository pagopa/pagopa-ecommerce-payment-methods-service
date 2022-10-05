package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Aggregate
public class Psp {
    private final PspCode pspCode;
    private final PspPaymentMethodType pspPaymentMethodType;
    private PspStatus pspStatus;
    private final PspBusinessName pspBusinessName;
    private final PspBrokerName pspBrokerName;
    private final PspDescription pspDescription;
    private final PspLanguage pspLanguage;

    private final PspAmount pspMinAmount;

    private final PspAmount pspMaxAmount;

    private final PspFee pspFixedCost;

    private final PspChannelCode pspChannelCode;


    @AggregateID
    public PspCode pspCode() {
        return this.pspCode;
    }

    public Psp(PspCode pspCode, PspPaymentMethodType pspPaymentMethodType, PspStatus pspStatus,
               PspBusinessName pspBusinessName, PspBrokerName pspBrokerName,
               PspDescription pspDescription, PspLanguage pspLanguage,
               PspAmount pspMinAmount, PspAmount pspMaxAmount,
               PspChannelCode pspChannelCode, PspFee pspFixedCost) {

        if (pspMinAmount.value().compareTo(pspMaxAmount.value()) > 0){
            throw new IllegalArgumentException("Invalid amount range");
        }

        this.pspCode = pspCode;
        this.pspPaymentMethodType = pspPaymentMethodType;
        this.pspStatus = pspStatus;
        this.pspBusinessName = pspBusinessName;
        this.pspBrokerName = pspBrokerName;
        this.pspDescription = pspDescription;
        this.pspLanguage = pspLanguage;
        this.pspMinAmount = pspMinAmount;
        this.pspMaxAmount = pspMaxAmount;
        this.pspChannelCode = pspChannelCode;
        this.pspFixedCost = pspFixedCost;
    }

    public void enablePsp(PspStatus pspStatus) {
        this.pspStatus = pspStatus;
    }
}
