package it.pagopa.ecommerce.payment.methods;

import it.pagopa.ecommerce.payment.methods.config.PreauthorizationUrlConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PreauthorizationUrlConfig.class)
public class PaymentMethodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMethodsApplication.class, args);
    }

}
