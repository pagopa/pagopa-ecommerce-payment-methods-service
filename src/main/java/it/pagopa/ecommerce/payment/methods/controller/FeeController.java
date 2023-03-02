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
        return feeService.computeFee(paymentOptionDto, maxOccurrences)
                .map(this::bundleOptionToResponse);

    }

    private ResponseEntity<BundleOptionDto> bundleOptionToResponse(
                                                                   it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto bundle
    ) {
        return ResponseEntity.ok(
                new BundleOptionDto()
                        .belowThreshold(bundle.getBelowThreshold())
                        .bundleOptions(
                                bundle.getBundleOptions() != null ? bundle.getBundleOptions()
                                        .stream()
                                        .map(
                                                t -> new TransferDto()
                                                        .abi(t.getAbi())
                                                        .bundleDescription(t.getBundleDescription())
                                                        .bundleName(t.getBundleName())
                                                        .idBrokerPsp(t.getIdBrokerPsp())
                                                        .idBundle(t.getIdBundle())
                                                        .idChannel(t.getIdChannel())
                                                        .idCiBundle(t.getIdCiBundle())
                                                        .idPsp(t.getIdPsp())
                                                        .onUs(t.getOnUs())
                                                        .paymentMethod(t.getPaymentMethod())
                                                        .primaryCiIncurredFee(t.getPrimaryCiIncurredFee())
                                                        .taxPayerFee(t.getTaxPayerFee())
                                                        .touchpoint(t.getTouchpoint())
                                        ).toList() : new ArrayList<>()
                        )
        );
    }
}
