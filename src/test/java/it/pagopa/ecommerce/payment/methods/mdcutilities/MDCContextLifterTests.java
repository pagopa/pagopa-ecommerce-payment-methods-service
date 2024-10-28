package it.pagopa.ecommerce.payment.methods.mdcutilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import reactor.util.context.Context;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MDCContextLifterTests {

    @InjectMocks
    private MDCContextLifter<Any> contextLifter;
    @Mock
    private Context context;

    private static final String TRANSACTION_ID_KEY = "transactionId";

    private static final String TRANSACTION_ID_VALUE = "transactionIdValue";

    @Test
    void MDCAndContextContainsTransactionId() {
        context = Context.of(TRANSACTION_ID_KEY, TRANSACTION_ID_VALUE);
        contextLifter.copyToMdc(context);
        assertTrue(MDC.getCopyOfContextMap().containsKey(TRANSACTION_ID_KEY));
        assertEquals(TRANSACTION_ID_VALUE, MDC.getCopyOfContextMap().get(TRANSACTION_ID_KEY));
    }

    @Test
    void contextIsEmpty() {
        context = Context.empty();
        contextLifter.copyToMdc(context);
        assertNull(MDC.getCopyOfContextMap());
    }
}
