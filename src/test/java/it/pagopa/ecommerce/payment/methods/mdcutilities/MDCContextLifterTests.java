package it.pagopa.ecommerce.payment.methods.mdcutilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import reactor.util.context.Context;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MDCContextLifterTests {

    @InjectMocks
    private MDCContextLifter<Any> contextLifter;
    @Mock
    private Context context;

    private static final String CONTEXT_KEY = "contextKey";

    private static final String CONTEXT_VALUE = "contextValue";

    private static final String TRANSACTION_ID_KEY = "transactionId";

    private static final String TRANSACTION_ID_VALUE = "transactionIdValue";

    @Test
    void onlyContextContainsContextKey() {
        context = Context.of(CONTEXT_KEY, CONTEXT_VALUE);
        contextLifter.copyToMdc(context);
        assertTrue(MDC.getCopyOfContextMap().containsKey(CONTEXT_KEY));
        assertEquals(MDC.getCopyOfContextMap().get(CONTEXT_KEY), CONTEXT_VALUE);
    }

    @Test
    void onlyMDCContainsContextKey() {
        MDC.setContextMap(Map.of(CONTEXT_KEY, CONTEXT_VALUE));
        contextLifter.copyToMdc(context);
        Map<String, String> contextMap = context.stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
        assertFalse(contextMap.containsKey(CONTEXT_KEY));
    }

    @Test
    void MDCAndContextContainsContextKey() {
        MDC.setContextMap(Map.of(CONTEXT_KEY, CONTEXT_VALUE, TRANSACTION_ID_KEY, TRANSACTION_ID_VALUE));
        context = Context.of(CONTEXT_KEY, CONTEXT_VALUE);
        contextLifter.copyToMdc(context);
        assertTrue(MDC.getCopyOfContextMap().containsKey(CONTEXT_KEY));
        assertTrue(MDC.getCopyOfContextMap().containsKey(TRANSACTION_ID_KEY));
        assertEquals(MDC.getCopyOfContextMap().get(TRANSACTION_ID_KEY), TRANSACTION_ID_VALUE);
    }

    @Test
    void contextIsEmpty() {
        MDC.setContextMap(Map.of(CONTEXT_KEY, CONTEXT_VALUE, TRANSACTION_ID_KEY, TRANSACTION_ID_VALUE));
        context = Context.empty();
        contextLifter.copyToMdc(context);
        assertNull(MDC.getCopyOfContextMap());
    }
}
