package it.pagopa.ecommerce.payment.methods.utils;

import it.pagopa.ecommerce.commons.client.NpgClient;

import java.util.Map;

/**
 * Utility class that maps payment type codes (e.g. "CP", "APPL") to
 * {@link NpgClient.PaymentMethod} instances.
 * <p>
 * This mapping is needed because the payment-methods-handler service returns
 * localized display names (e.g. "Carte", "PayPal") that do not match the NPG
 * service names (e.g. "CARDS", "PAYPAL"). The payment type code is a stable
 * identifier that can be reliably mapped.
 * </p>
 */
public final class NpgPaymentMethodMapping {

    private NpgPaymentMethodMapping() {
        // utility class
    }

    private static final Map<String, String> PAYMENT_TYPE_CODE_TO_SERVICE_NAME = Map.ofEntries(
            Map.entry("CP", "CARDS"),
            Map.entry("APPL", "APPLEPAY"),
            Map.entry("GOOG", "GOOGLEPAY"),
            Map.entry("PPAL", "PAYPAL"),
            Map.entry("BPAY", "BANCOMATPAY"),
            Map.entry("MYBK", "MYBANK"),
            Map.entry("SATY", "SATISPAY_DIRECT")
    );

    /**
     * Resolves a {@link NpgClient.PaymentMethod} from a payment type code.
     *
     * @param paymentTypeCode the payment type code (e.g. "CP", "APPL")
     * @return the corresponding {@link NpgClient.PaymentMethod}
     * @throws IllegalArgumentException if no mapping exists for the given code
     */
    public static NpgClient.PaymentMethod fromPaymentTypeCode(String paymentTypeCode) {
        if (paymentTypeCode == null) {
            throw new IllegalArgumentException("Payment type code must not be null");
        }
        String serviceName = PAYMENT_TYPE_CODE_TO_SERVICE_NAME.get(paymentTypeCode);
        if (serviceName == null) {
            throw new IllegalArgumentException(
                    "No NPG service name mapping for payment type code: '%s'".formatted(paymentTypeCode)
            );
        }
        return NpgClient.PaymentMethod.fromServiceName(serviceName);
    }
}
