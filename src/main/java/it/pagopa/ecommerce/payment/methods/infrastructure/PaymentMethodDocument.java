package it.pagopa.ecommerce.payment.methods.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;

import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "payment-methods")
public class PaymentMethodDocument {

    @Id
    private String paymentMethodID;
    private String paymentMethodName;
    private String paymentMethodDescription;
    private String paymentMethodStatus;
    private String paymentMethodAsset;
    private List<Pair<Long, Long>> paymentMethodRanges;
    private String paymentMethodTypeCode;
    private String clientId;
}
