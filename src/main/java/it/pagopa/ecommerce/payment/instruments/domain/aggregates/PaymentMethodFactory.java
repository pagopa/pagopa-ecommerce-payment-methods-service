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
@AggregateFactory(PaymentMethod.class)
public class PaymentMethodFactory {

    @Autowired
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @Autowired
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @AggregateFactory(PaymentMethod.class)
    public Mono<PaymentMethod> newPaymentInstrument(PaymentMethodID paymentMethodID,
                                                    PaymentMethodName paymentMethodName,
                                                    PaymentMethodDescription paymentMethodDescription,
                                                    PaymentMethodStatus paymentInstrumentEnabled,
                                                    PaymentInstrumentCategoryID paymentInstrumentCategoryID,
                                                    PaymentMethodType paymentMethodTypeCode) {

        return paymentInstrumentRepository.findByPaymentInstrumentName(paymentMethodName.value()).hasElements()
                .flatMap(hasPaymentInstrument -> paymentInstrumentCategoryRepository
                        .findById(paymentInstrumentCategoryID.value().toString()).switchIfEmpty(
                                Mono.error(categoryNotFoundException(paymentInstrumentCategoryID))
                        ).map(
                                category -> {
                                    if (Boolean.FALSE.equals(hasPaymentInstrument) && Boolean.TRUE.equals(category != null)) {
                                        return new PaymentMethod(paymentMethodID, paymentMethodName,
                                                paymentMethodDescription,
                                                paymentInstrumentEnabled, paymentInstrumentCategoryID,
                                                new PaymentInstrumentCategoryName(category.getPaymentInstrumentCategoryName()),
                                                category.getPaymentInstrumentCategoryTypes().stream().map(
                                                        PaymentMethodType::new
                                                ).collect(Collectors.toList()),
                                                paymentMethodTypeCode);
                                    } else if (Boolean.TRUE.equals(hasPaymentInstrument)) {
                                        throw paymentInstrumentAlreadyInUse(paymentMethodName);
                                    } else {
                                        throw categoryNotFoundException(paymentInstrumentCategoryID);
                                    }
                                }
                        ));
    }
}