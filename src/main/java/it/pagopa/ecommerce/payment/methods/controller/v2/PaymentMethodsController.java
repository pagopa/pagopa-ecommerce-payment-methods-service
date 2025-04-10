package it.pagopa.ecommerce.payment.methods.controller.v2;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import it.pagopa.ecommerce.payment.methods.application.v2.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.exception.AfmResponseException;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.ProblemJsonDto;
import it.pagopa.ecommerce.payment.methods.v2.server.api.V2Api;
import it.pagopa.ecommerce.payment.methods.v2.server.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController("paymentMethodsControllerV2")
@Slf4j
public class PaymentMethodsController implements V2Api {

    private final PaymentMethodService paymentMethodService;

    private final it.pagopa.ecommerce.payment.methods.controller.v1.PaymentMethodsController paymentMethodsControllerV1;

    @Value("${warmup.payment.method.id}")
    String warmupPaymentMethodID;

    public PaymentMethodsController(
            PaymentMethodService paymentMethodService,
            it.pagopa.ecommerce.payment.methods.controller.v1.PaymentMethodsController paymentMethodsControllerV1
    ) {
        this.paymentMethodService = paymentMethodService;
        this.paymentMethodsControllerV1 = paymentMethodsControllerV1;
    }

    @Override
    public Mono<ResponseEntity<CalculateFeeResponseDto>> calculateFees(
                                                                       String id,
                                                                       Mono<CalculateFeeRequestDto> calculateFeeRequestDto,
                                                                       Integer maxOccurrences,
                                                                       ServerWebExchange exchange
    ) {
        return calculateFeeRequestDto
                .flatMap(feeRequestDto -> paymentMethodService.computeFee(feeRequestDto, id, maxOccurrences))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SessionGetTransactionIdResponseDto>> getTransactionIdForSession(
                                                                                               String id,
                                                                                               String orderId,
                                                                                               ServerWebExchange exchange
    ) {
        return paymentMethodsControllerV1
                .getTransactionIdAssociatedToNpgSession(id, orderId, exchange)
                .map(
                        transactionId -> new SessionGetTransactionIdResponseDto()
                                .transactionId(transactionId.value())
                                .base64EncodedTransactionId(transactionId.base64())
                )
                .map(ResponseEntity::ok);
    }

    @ExceptionHandler(
        {
                AfmResponseException.class,
                NoBundleFoundException.class,
                PaymentMethodNotFoundException.class
        }
    )
    public ResponseEntity<ProblemJsonDto> errorHandler(RuntimeException exception) {
        String notFoundTitle = "Not found";

        log.error("Got exception", exception);

        if (exception instanceof PaymentMethodNotFoundException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title(notFoundTitle).detail("Payment method not found"),
                    HttpStatus.NOT_FOUND
            );
        } else if (exception instanceof AfmResponseException afmException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(afmException.status.value())
                            .title("Afm generic error")
                            .detail(afmException.reason),
                    afmException.status
            );
        } else if (exception instanceof NoBundleFoundException ex) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title(notFoundTitle).detail(ex.getMessage())
                            .detail(exception.getMessage()),
                    HttpStatus.NOT_FOUND
            );
        } else {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(500).title("Internal server error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Warmup
    public void calculateFeesWarmupMethod() {
        CalculateFeeRequestDto request = new CalculateFeeRequestDto()
                .touchpoint("CHECKOUT")
                .isAllCCP(false)
                .addPaymentNoticesItem(
                        new PaymentNoticeDto()
                                .paymentAmount(100L)
                                .primaryCreditorInstitution("77777777777")
                                .addTransferListItem(
                                        new TransferListItemDto()
                                                .digitalStamp(false)
                                                .creditorInstitution("77777777777")
                                )

                );
        WebClient
                .create()
                .post()
                .uri(
                        "http://localhost:8080/v2/payment-methods/{id}/fees",
                        warmupPaymentMethodID
                )
                .bodyValue(request)
                .header("X-Client-ID", PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.toString())
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(30));
    }

}
