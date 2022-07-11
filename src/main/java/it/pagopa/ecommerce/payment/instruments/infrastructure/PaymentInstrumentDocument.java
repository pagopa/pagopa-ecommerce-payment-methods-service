package it.pagopa.ecommerce.payment.instruments.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "payment-instruments")
public class PaymentInstrumentDocument {

    @Id
    private String paymentInstrumentID;
    private String paymentInstrumentName;
    private String paymentInstrumentDescription;
    private String paymentInstrumentStatus;
    private String paymentInstrumentCategoryID;
    private String paymentInstrumentCategoryName;
    private List<String> paymentInstrumentCategoryTypes;
    private String paymentInstrumentTypeCode;
}
