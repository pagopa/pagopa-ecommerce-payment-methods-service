package it.pagopa.ecommerce.payment.instruments.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PspDocumentKey implements Serializable {
    private String pspCode;
    private String pspPaymentTypeCode;
    private String pspChannelCode;
}
