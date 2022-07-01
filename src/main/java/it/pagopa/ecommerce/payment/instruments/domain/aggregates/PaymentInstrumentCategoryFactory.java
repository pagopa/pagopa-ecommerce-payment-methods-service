package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryID;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryName;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentType;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.ecommerce.payment.instruments.exception.CategoryAlreadyInUseException.categoryAlreadyInUseException;

@Component
@AggregateFactory(PaymentInstrumentCategory.class)
public class PaymentInstrumentCategoryFactory {

    @Autowired
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @AggregateFactory(PaymentInstrumentCategory.class)
    public Mono<PaymentInstrumentCategory> newCategory(PaymentInstrumentCategoryID paymentInstrumentCategoryID,
                                                       PaymentInstrumentCategoryName paymentInstrumentCategoryName,
                                                       List<PaymentInstrumentType> paymentInstrumentTypes) {

        return paymentInstrumentCategoryRepository.findByPaymentInstrumentCategoryName(paymentInstrumentCategoryName.value()).hasElement()
                .map(hasCategory -> {
                    if (!hasCategory) {
                        return new PaymentInstrumentCategory(
                                paymentInstrumentCategoryID, paymentInstrumentTypes, paymentInstrumentCategoryName);
                    } else {
                        throw categoryAlreadyInUseException(paymentInstrumentCategoryName);
                    }
                });
    }

}
