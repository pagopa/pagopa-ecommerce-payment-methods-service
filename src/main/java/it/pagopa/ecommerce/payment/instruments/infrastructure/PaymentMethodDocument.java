package it.pagopa.ecommerce.payment.instruments.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
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
    @Indexed(unique = true)
    private String paymentMethodName;
    private String paymentMethodDescription;
    private String paymentMethodStatus;
    private List<Pair<Long, Long>> paymentMethodRanges;
    @Indexed(unique = true)
    private String paymentMethodTypeCode;
}
