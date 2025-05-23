package it.pagopa.ecommerce.payment.methods;

import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(SessionUrlConfig.class)
@ComponentScan(basePackages = {
        "it.pagopa.ecommerce.payment.methods",
        "it.pagopa.ecommerce.commons"   // More restrictive classloader and component scanning configuration
})
public class PaymentMethodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMethodsApplication.class, args);
    }

}
