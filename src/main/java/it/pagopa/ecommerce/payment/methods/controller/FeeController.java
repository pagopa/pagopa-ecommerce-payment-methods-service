package it.pagopa.ecommerce.payment.methods.controller;

import it.pagopa.ecommerce.payment.methods.application.FeeService;
import it.pagopa.ecommerce.payment.methods.exception.AfmResponseException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodAlreadyInUseException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.server.api.FeeApi;
import it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentOptionDto;
import it.pagopa.ecommerce.payment.methods.server.model.ProblemJsonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class FeeController implements FeeApi {

    @Autowired
    private FeeService feeService;

    @ExceptionHandler(
        {
                AfmResponseException.class,
                Exception.class,
                ResponseStatusException.class
        }
    )
    public ResponseEntity<ProblemJsonDto> errorHandler(RuntimeException exception) {
        if (exception instanceof AfmResponseException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(((AfmResponseException) exception).status.value())
                            .title("Afm generic error")
                            .detail(((AfmResponseException) exception).reason),
                    ((AfmResponseException) exception).status
            );
        } else {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(500).title("Internal server error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public Mono<ResponseEntity<BundleOptionDto>> calculateFees(
                                                               Mono<PaymentOptionDto> paymentOptionDto,
                                                               Integer maxOccurrences,
                                                               ServerWebExchange exchange
    ) {
        return feeService.computeFee(paymentOptionDto, maxOccurrences).map(
                ResponseEntity::ok
        );
    }
}
