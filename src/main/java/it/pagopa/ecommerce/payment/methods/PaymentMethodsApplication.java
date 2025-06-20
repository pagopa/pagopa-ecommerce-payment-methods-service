package it.pagopa.ecommerce.payment.methods;

import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SessionUrlConfig.class)
public class PaymentMethodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMethodsApplication.class, args);
    }

}
