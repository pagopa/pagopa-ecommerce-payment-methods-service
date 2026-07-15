package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.commons.client.NpgClient;
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

import java.util.Map;

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

    /**
     * Resolves the payment method name for the fees response. For NPG-managed
     * methods (CARDS, PAYPAL, etc.) returns the enum constant name. For redirect
     * methods (RBPR, RBPS, etc.) that are not in the NPG enum, returns the
     * localized name from the handler response.
     */
    protected String resolvePaymentMethodName(
                                              String paymentTypeCode,
                                              Map<String, String> nameMap
    ) {
        try {
            return NpgClient.PaymentMethod.fromMethodTypeCode(paymentTypeCode).name();
        } catch (IllegalArgumentException e) {
            return resolveLocalizedValue(nameMap);
        }
    }

    /**
     * Extracts a localized description from a map, falling back to the first
     * available value or empty string.
     */
    protected String resolveLocalizedValue(Map<String, String> localizedMap) {
        return localizedMap.getOrDefault("it", localizedMap.values().stream().findFirst().orElse(""));
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
