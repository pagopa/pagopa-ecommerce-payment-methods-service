package it.pagopa.ecommerce.payment.methods;

import it.pagopa.ecommerce.commons.utils.v2.JwtTokenUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
class PaymentMethodsApplicationTests {

    @MockBean
    private JwtTokenUtils jwtTokenUtils;

    @Test
    void contextLoads() {
    }

}
