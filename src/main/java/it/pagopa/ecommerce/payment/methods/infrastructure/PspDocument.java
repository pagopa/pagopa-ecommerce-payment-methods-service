package it.pagopa.ecommerce.payment.methods.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;


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
    private BigInteger pspMinAmount;
    private BigInteger pspMaxAmount;
    private BigInteger pspFixedCost;
}
