package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.commons.domain.v2.TransactionId;
import it.pagopa.ecommerce.payment.methods.exception.InvalidSessionException;
import it.pagopa.ecommerce.payment.methods.exception.MismatchedSecurityTokenException;
import it.pagopa.ecommerce.payment.methods.exception.OrderIdNotFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class PaymentMethodServiceCommon {

    private final NpgSessionsTemplateWrapper npgSessionsTemplateWrapper;

    protected PaymentMethodServiceCommon(
            NpgSessionsTemplateWrapper npgSessionsTemplateWrapper
    ) {
        this.npgSessionsTemplateWrapper = npgSessionsTemplateWrapper;
    }

    public Mono<TransactionId> isSessionValid(
                                              String orderId,
                                              String securityToken
    ) {
        return npgSessionsTemplateWrapper.findById(orderId)
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
