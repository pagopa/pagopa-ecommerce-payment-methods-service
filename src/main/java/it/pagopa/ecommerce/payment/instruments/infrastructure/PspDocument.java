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
    private String code;
    private String paymentInstrumentID;
    private String status;
    private String type;
    private String PSPName;
    private String brokerName;
    private String description;
    private List<String> languages;
    private List<String> ranges;
}
