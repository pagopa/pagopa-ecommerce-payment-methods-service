package it.pagopa.ecommerce.payment.methods.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UniqueIdUtilsTests {

    @Test
    void generateUniqueIdCorrectly() {
        String uniqueId = new UniqueIdUtils().generateUniqueId();
        assertEquals(uniqueId.length(), 18);
    }
}
