package it.pagopa.ecommerce.payment.instruments.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(collection = "payment-instruments")
public class PaymentInstrumentDocument {

    @Id
    private String paymentInstrumentID;
    private String paymentInstrumentName;
    private String paymentInstrumentDescription;
    private String paymentInstrumentType;
    private String paymentInstrumentStatus;
}
