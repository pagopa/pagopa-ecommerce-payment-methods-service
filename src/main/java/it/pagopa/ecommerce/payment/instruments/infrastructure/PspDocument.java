package it.pagopa.ecommerce.payment.instruments.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "psps")
public class PspDocument {

    @Id
    private String pspCode;
    private String paymentInstrumentID;
    private String pspStatus;
    private String pspType;
    private String pspName;
    private String pspBrokerName;
    private String pspDescription;
    private List<String> pspLanguages;
    private List<String> pspRanges;
}
