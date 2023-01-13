package it.pagopa.ecommerce.payment.methods.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@AllArgsConstructor
@Document(collection = "psps")
public class PspDocument {

    @Id
    private PspDocumentKey pspDocumentKey;
    private String pspStatus;
    private String pspBusinessName;
    private String pspBrokerName;
    private String pspDescription;
    private long pspMinAmount;
    private long pspMaxAmount;
    private long pspFixedCost;
}
