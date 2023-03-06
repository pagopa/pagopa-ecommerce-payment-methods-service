package it.pagopa.ecommerce.payment.methods.controller;

import it.pagopa.ecommerce.payment.methods.application.FeeService;
import it.pagopa.ecommerce.payment.methods.server.api.FeeApi;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class FeeController implements FeeApi {

    @Autowired
    FeeService feeService;

    @Override
    public Mono<ResponseEntity<BundleOptionDto>> calculateFees(
                                                               Mono<PaymentOptionDto> paymentOptionDto,
                                                               Integer maxOccurrences,
                                                               ServerWebExchange exchange
    ) {
        return feeService.computeFee(paymentOptionDto, maxOccurrences).map(
                resp -> ResponseEntity.ok(resp)
        );
    }
}
