package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.commons.client.NpgClient;
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

    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because this method wraps `PaymentMethod` constructor.
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S107")
    @AggregateFactory(PaymentMethod.class)
    public Mono<PaymentMethod> newPaymentMethod(
                                                PaymentMethodID paymentMethodID,
                                                PaymentMethodName paymentMethodName,
                                                PaymentMethodDescription paymentMethodDescription,
                                                PaymentMethodStatus paymentMethodStatus,
                                                List<PaymentMethodRange> paymentMethodRanges,
                                                PaymentMethodType paymentMethodTypeCode,
                                                PaymentMethodAsset paymentMethodAsset,
                                                NpgClient.PaymentMethod npgPaymentMethod
    ) {

        return paymentMethodRepository.findByPaymentMethodNameOrPaymentMethodTypeCode(
                paymentMethodName.value(),
                paymentMethodTypeCode.value()
        ).hasElement()
                .map(hasPaymentMethod -> {
                    if (Boolean.TRUE.equals(hasPaymentMethod)) {
                        throw paymentMethodAlreadyInUse(paymentMethodName);
                    }
                    return new PaymentMethod(
                            paymentMethodID,
                            paymentMethodName,
                            paymentMethodDescription,
                            paymentMethodStatus,
                            paymentMethodTypeCode,
                            paymentMethodRanges,
                            paymentMethodAsset,
                            npgPaymentMethod
                    );
                }
                );
    }
}
