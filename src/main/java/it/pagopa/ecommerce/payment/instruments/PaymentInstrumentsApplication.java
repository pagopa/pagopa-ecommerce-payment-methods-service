package it.pagopa.ecommerce.payment.instruments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentInstrumentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentInstrumentsApplication.class, args);
	}

}
