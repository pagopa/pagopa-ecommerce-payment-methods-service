package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import static it.pagopa.ecommerce.payment.instruments.exception.PaymentMethodAlreadyInUseException.paymentInstrumentAlreadyInUse;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodRepository;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AggregateFactory(PaymentMethod.class)
public class PaymentMethodFactory {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;


    @AggregateFactory(PaymentMethod.class)
    public Mono<PaymentMethod> newPaymentMethod(PaymentMethodID paymentMethodID,
                                                PaymentMethodName paymentMethodName,
                                                PaymentMethodDescription paymentMethodDescription,
                                                PaymentMethodStatus paymentMethodStatus,
                                                List<PaymentMethodRange> paymentMethodRanges,
                                                PaymentMethodType paymentMethodTypeCode) {

        return paymentMethodRepository.findByPaymentMethodNameOrPaymentMethodTypeCode(
                    paymentMethodName.value(), paymentMethodTypeCode.value()).hasElement()
                .map(hasPaymentInstrument -> {
                            if (Boolean.TRUE.equals(hasPaymentInstrument)) {
                                throw paymentInstrumentAlreadyInUse(paymentMethodName);
                            }

                            return new PaymentMethod(paymentMethodID, paymentMethodName,
                                    paymentMethodDescription,
                                    paymentMethodStatus, paymentMethodRanges, paymentMethodTypeCode);
                        }
                );
    }
}