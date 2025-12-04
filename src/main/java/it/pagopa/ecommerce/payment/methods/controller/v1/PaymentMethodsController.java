package it.pagopa.ecommerce.payment.methods.controller.v1;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.payment.methods.application.v1.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.exception.*;
import it.pagopa.ecommerce.payment.methods.server.api.PaymentMethodsApi;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import static it.pagopa.ecommerce.payment.methods.utils.HttpUtils.getAuthenticationToken;

@RestController
@Slf4j
public class PaymentMethodsController implements PaymentMethodsApi {

    @Autowired
    private PaymentMethodService paymentMethodService;

    private static final String X_CLIENT_ID = "X-Client-ID";
    private static final String X_API_KEY = "x-api-key";

    @Value("${warmup.payment.method.id}")
    String warmupPaymentMethodID;
    @Value("${security.apiKey.primary}")
    String primaryApiKey;

    @ExceptionHandler(
        {
                PaymentMethodAlreadyInUseException.class,
                PaymentMethodNotFoundException.class,
                OrderIdNotFoundException.class,
                UniqueIdGenerationException.class,
                AfmResponseException.class,
                InvalidSessionException.class,
                MismatchedSecurityTokenException.class,
                SessionAlreadyAssociatedToTransaction.class,
                NoBundleFoundException.class,
                JwtIssuerResponseException.class,
                NpgResponseException.class
        }
    )
    public ResponseEntity<ProblemJsonDto> errorHandler(RuntimeException exception) {
        String notFoundTitle = "Not found";

        log.error("Got exception", exception);

        if (exception instanceof PaymentMethodAlreadyInUseException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(400).title("Bad request").detail("Payment method already in use"),
                    HttpStatus.BAD_REQUEST
            );
        } else if (exception instanceof PaymentMethodNotFoundException) {
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
        } else if (exception instanceof OrderIdNotFoundException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title(notFoundTitle).detail("Order id not found"),
                    HttpStatus.NOT_FOUND
            );
        } else if (exception instanceof UniqueIdGenerationException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(500).title("Internal system error").detail(exception.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } else if (exception instanceof MismatchedSecurityTokenException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title(notFoundTitle).detail("Order id not found"),
                    HttpStatus.NOT_FOUND
            );
        } else if (exception instanceof InvalidSessionException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(409).title("Invalid session").detail("Invalid session"),
                    HttpStatus.CONFLICT
            );
        } else if (exception instanceof SessionAlreadyAssociatedToTransaction) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(409).title("Session already associated to transaction")
                            .detail(exception.getMessage()),
                    HttpStatus.CONFLICT
            );
        } else if (exception instanceof NpgResponseException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(502).title("Bad Gateway")
                            .detail(exception.getMessage()),
                    HttpStatus.BAD_GATEWAY
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemJsonDto> illegalArgumentExceptionHandler(IllegalArgumentException exception) {
        return new ResponseEntity<>(
                new ProblemJsonDto()
                        .status(400)
                        .title("Bad request")
                        .detail(exception.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodsResponseDto>> getAllPaymentMethods(
                                                                                String xClientId,
                                                                                BigDecimal amount,
                                                                                ServerWebExchange exchange
    ) {
        return paymentMethodService.retrievePaymentMethods(amount != null ? amount.longValue() : null, xClientId)
                .map(PaymentMethodsController::paymentMethodToDto)
                .collectList()
                .map(
                        paymentMethods -> ResponseEntity.ok(
                                new PaymentMethodsResponseDto()
                                        .paymentMethods(paymentMethods)
                        )
                );
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodResponseDto>> getPaymentMethod(
                                                                           String id,
                                                                           String xClientId,
                                                                           ServerWebExchange exchange
    ) {
        return paymentMethodService.retrievePaymentMethodById(id, xClientId)
                .map(this::paymentMethodToResponse);
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodResponseDto>> newPaymentMethod(
                                                                           @Valid Mono<PaymentMethodRequestDto> paymentMethodRequestDto,
                                                                           ServerWebExchange exchange
    ) {
        return paymentMethodRequestDto.flatMap(
                request -> paymentMethodService.createPaymentMethod(request)
                        .map(this::paymentMethodToResponse)
        );
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodResponseDto>> patchPaymentMethod(
                                                                             String id,
                                                                             Mono<PatchPaymentMethodRequestDto> patchPaymentMethodRequestDto,
                                                                             ServerWebExchange exchange
    ) {
        return patchPaymentMethodRequestDto
                .flatMap(
                        request -> paymentMethodService
                                .updatePaymentMethodStatus(
                                        id,
                                        PaymentMethodStatusEnum.valueOf(request.getStatus().toString())
                                )
                                .map(this::paymentMethodToResponse)
                );
    }

    private ResponseEntity<PaymentMethodResponseDto> paymentMethodToResponse(PaymentMethod paymentMethod) {
        PaymentMethodResponseDto response = paymentMethodToDto(paymentMethod);
        return ResponseEntity.ok(response);
    }

    private static PaymentMethodResponseDto paymentMethodToDto(PaymentMethod paymentMethod) {
        PaymentMethodResponseDto response = new PaymentMethodResponseDto();
        response.setId(paymentMethod.getPaymentMethodID().value().toString());
        response.setName(paymentMethod.getPaymentMethodName().value());
        response.setDescription(paymentMethod.getPaymentMethodDescription().value());
        response.setStatus(
                PaymentMethodStatusDto.valueOf(
                        paymentMethod.getPaymentMethodStatus().value().toString()
                )
        );
        response.setRanges(
                paymentMethod.getPaymentMethodRanges().stream().map(
                        r -> {
                            RangeDto rangeDto = new RangeDto();
                            rangeDto.setMin(r.min());
                            rangeDto.setMax(r.max());
                            return rangeDto;
                        }
                ).collect(Collectors.toList())
        );
        response.setPaymentTypeCode(paymentMethod.getPaymentMethodTypeCode().value());
        response.setAsset(paymentMethod.getPaymentMethodAsset().value());
        response.setMethodManagement(
                paymentMethod.getPaymentMethodManagement().value()
        );
        response.setBrandAssets(paymentMethod.getPaymentMethodBrandAsset().brandAssets().orElse(null));
        return response;
    }

    @Override
    public Mono<ResponseEntity<CalculateFeeResponseDto>> calculateFees(
                                                                       String id,
                                                                       Mono<CalculateFeeRequestDto> calculateFeeRequestDto,
                                                                       Integer maxOccurrences,
                                                                       ServerWebExchange exchange
    ) {

        return calculateFeeRequestDto.flatMap(request -> paymentMethodService.computeFee(request, id, maxOccurrences))
                .map(
                        ResponseEntity::ok
                );
    }

    @Override
    public Mono<ResponseEntity<CreateSessionResponseDto>> createSession(
                                                                        String id,
                                                                        String lang,
                                                                        ClientIdDto xClientId,
                                                                        ServerWebExchange exchange
    ) {
        return paymentMethodService.createSessionForPaymentMethod(id, lang, xClientId).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SessionPaymentMethodResponseDto>> getSessionPaymentMethod(
                                                                                         String id,
                                                                                         String orderId,
                                                                                         ServerWebExchange exchange
    ) {
        log.info("[Payment Method controller] Retrieve card data from NPG");
        return paymentMethodService.getCardDataInformation(id, orderId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SessionGetTransactionIdResponseDto>> getTransactionIdForSession(
                                                                                               String id,
                                                                                               String orderId,
                                                                                               ServerWebExchange exchange
    ) {
        return getAuthenticationToken(exchange)
                .doOnNext(
                        req -> log.info(
                                "Requesting session validation for paymentMethodId={}, orderId={}",
                                id,
                                orderId
                        )
                )
                .flatMap(securityToken -> paymentMethodService.isSessionValid(id, orderId, securityToken))
                .map(transactionId -> new SessionGetTransactionIdResponseDto().transactionId(transactionId.base64()))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> updateSession(
                                                    String id,
                                                    String orderId,
                                                    Mono<PatchSessionRequestDto> patchSessionRequestDto,
                                                    ServerWebExchange exchange
    ) {
        return patchSessionRequestDto
                .flatMap(updateData -> paymentMethodService.updateSession(id, orderId, updateData))
                .map(ignored -> ResponseEntity.noContent().build());
    }

    @Warmup
    public void getAllPaymentMethodsWarmupMethod() {
        WebClient
                .create()
                .get()
                .uri("http://localhost:8080/payment-methods")
                .header(X_CLIENT_ID, PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.toString())
                .header(X_API_KEY, primaryApiKey)
                .retrieve()
                .bodyToMono(PaymentMethodsResponseDto.class)
                .block(Duration.ofSeconds(30));
    }

    @Warmup
    public void calculateFeesWarmupMethod() {
        CalculateFeeRequestDto request = new CalculateFeeRequestDto()
                .touchpoint("touchpoint1")
                .paymentAmount(1L)
                .primaryCreditorInstitution("77777777777")
                .transferList(Collections.emptyList())
                .isAllCCP(false);
        WebClient
                .create()
                .post()
                .uri(
                        "http://localhost:8080/payment-methods/{id}/fees",
                        UUID.randomUUID().toString()
                )
                .bodyValue(request)
                .header(X_CLIENT_ID, PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.toString())
                .header(X_API_KEY, primaryApiKey)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(30));
    }

    @Warmup
    public void createSessionWarmupMethod() {
        WebClient
                .create()
                .post()
                .uri(
                        "http://localhost:8080/payment-methods/{id}/sessions",
                        warmupPaymentMethodID
                )
                .header(X_CLIENT_ID, PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.toString())
                .header(X_API_KEY, primaryApiKey)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(30));
    }
}
