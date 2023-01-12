package it.pagopa.ecommerce.payment.methods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentMethodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMethodsApplication.class, args);
    }

}
