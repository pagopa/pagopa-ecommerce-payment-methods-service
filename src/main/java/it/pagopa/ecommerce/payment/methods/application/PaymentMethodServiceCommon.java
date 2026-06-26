package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.commons.domain.v2.TransactionId;
import it.pagopa.ecommerce.payment.methods.client.PaymentMethodsHandlerClient;
import it.pagopa.ecommerce.payment.methods.exception.InvalidSessionException;
import it.pagopa.ecommerce.payment.methods.exception.MismatchedSecurityTokenException;
import it.pagopa.ecommerce.payment.methods.exception.OrderIdNotFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.server.model.ClientIdDto;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class PaymentMethodServiceCommon {

    private final NpgSessionsTemplateWrapper npgSessionsTemplateWrapper;
    private final PaymentMethodsHandlerClient paymentMethodsHandlerClient;

    protected PaymentMethodServiceCommon(
            NpgSessionsTemplateWrapper npgSessionsTemplateWrapper,
            PaymentMethodsHandlerClient paymentMethodsHandlerClient
    ) {
        this.npgSessionsTemplateWrapper = npgSessionsTemplateWrapper;
        this.paymentMethodsHandlerClient = paymentMethodsHandlerClient;
    }

    public Mono<TransactionId> isSessionValid(
                                              String paymentMethodId,
                                              String orderId,
                                              String securityToken,
                                              ClientIdDto xClientId
    ) {
        return paymentMethodsHandlerClient
                .validatePaymentMethodExists(paymentMethodId, xClientId != null ? xClientId.getValue() : null)
                .then(npgSessionsTemplateWrapper.findById(orderId))
                .switchIfEmpty(Mono.error(new OrderIdNotFoundException(orderId)))
                .flatMap(doc -> {
                    String transactionId = doc.transactionId();
                    if (transactionId == null) {
                        return Mono.error(new InvalidSessionException(orderId));
                    } else {
                        return Mono.just(doc);
                    }
                })
                .flatMap(doc -> {
                    if (!doc.securityToken().equals(securityToken)) {
                        log.warn("Invalid security token for requested order id {}", orderId);
                        return Mono.error(new MismatchedSecurityTokenException(orderId, doc.transactionId()));
                    } else {
                        return Mono.just(doc);
                    }
                })
                .mapNotNull(NpgSessionDocument::transactionId)
                .map(TransactionId::new);
    }
}
