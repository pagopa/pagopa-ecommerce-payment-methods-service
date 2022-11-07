package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodAsset;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodID;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodRange;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodStatus;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodType;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.ecommerce.payment.methods.exception.PaymentMethodAlreadyInUseException.paymentMethodAlreadyInUse;

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
                                                PaymentMethodType paymentMethodTypeCode,
                                                PaymentMethodAsset paymentMethodAsset) {

        return paymentMethodRepository.findByPaymentMethodName(paymentMethodName.value()).hasElements()
                .map(hasPaymentMethod -> {
                            if (Boolean.TRUE.equals(hasPaymentMethod)) {
                                throw paymentMethodAlreadyInUse(paymentMethodName);
                            }

                            return new PaymentMethod(paymentMethodID, paymentMethodName,
                                    paymentMethodDescription, paymentMethodStatus,
                                    paymentMethodRanges, paymentMethodTypeCode, paymentMethodAsset);
                        }
                );
    }
}