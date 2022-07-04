package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import static it.pagopa.ecommerce.payment.instruments.exception.CategoryNotFoundException.categoryNotFoundException;
import static it.pagopa.ecommerce.payment.instruments.exception.PaymentInstrumentAlreadyInUseException.paymentInstrumentAlreadyInUse;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentRepository;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@AggregateFactory(PaymentInstrument.class)
public class PaymentInstrumentFactory {

    @Autowired
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @Autowired
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @AggregateFactory(PaymentInstrument.class)
    public Mono<PaymentInstrument> newPaymentInstrument(PaymentInstrumentID paymentInstrumentID,
                                                        PaymentInstrumentName paymentInstrumentName,
                                                        PaymentInstrumentDescription paymentInstrumentDescription,
                                                        PaymentInstrumentStatus paymentInstrumentEnabled,
                                                        PaymentInstrumentCategoryID paymentInstrumentCategoryID) {

        return paymentInstrumentRepository.findByPaymentInstrumentName(paymentInstrumentName.value()).hasElements()
                .flatMap(hasPaymentInstrument -> paymentInstrumentCategoryRepository
                        .findById(paymentInstrumentCategoryID.value().toString()).switchIfEmpty(
                                Mono.error(categoryNotFoundException(paymentInstrumentCategoryID))
                        ).map(
                                category -> {
                                    if (Boolean.FALSE.equals(hasPaymentInstrument) && Boolean.TRUE.equals(category != null)) {
                                        return new PaymentInstrument(paymentInstrumentID, paymentInstrumentName,
                                                paymentInstrumentDescription,
                                                paymentInstrumentEnabled, paymentInstrumentCategoryID,
                                                new PaymentInstrumentCategoryName(category.getPaymentInstrumentCategoryName()),
                                                category.getPaymentInstrumentCategoryTypes().stream().map(
                                                        PaymentInstrumentType::new
                                                ).collect(Collectors.toList()));
                                    } else if (Boolean.TRUE.equals(hasPaymentInstrument)) {
                                        throw paymentInstrumentAlreadyInUse(paymentInstrumentName);
                                    } else {
                                        throw categoryNotFoundException(paymentInstrumentCategoryID);
                                    }
                                }
                        ));
    }
}