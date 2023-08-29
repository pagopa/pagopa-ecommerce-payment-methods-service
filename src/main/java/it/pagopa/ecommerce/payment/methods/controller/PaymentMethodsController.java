package it.pagopa.ecommerce.payment.methods.controller;

import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.exception.AfmResponseException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodAlreadyInUseException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.exception.SessionIdNotFoundException;
import it.pagopa.ecommerce.payment.methods.server.api.PaymentMethodsApi;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class PaymentMethodsController implements PaymentMethodsApi {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @ExceptionHandler(
        {
                PaymentMethodAlreadyInUseException.class,
                PaymentMethodNotFoundException.class,
                SessionIdNotFoundException.class,
                AfmResponseException.class
        }
    )
    public ResponseEntity<ProblemJsonDto> errorHandler(RuntimeException exception) {
        if (exception instanceof PaymentMethodAlreadyInUseException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title("Bad request").detail("Payment method already in use"),
                    HttpStatus.BAD_REQUEST
            );
        } else if (exception instanceof PaymentMethodNotFoundException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title("Not found").detail("Payment method not found"),
                    HttpStatus.NOT_FOUND
            );
        } else if (exception instanceof AfmResponseException afmException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(afmException.status.value())
                            .title("Afm generic error")
                            .detail(afmException.reason),
                    afmException.status
            );
        } else if (exception instanceof SessionIdNotFoundException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title("Not found").detail("Session id not found"),
                    HttpStatus.NOT_FOUND
            );
        } else {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(500).title("Internal server error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodsResponseDto>> getAllPaymentMethods(
                                                                                BigDecimal amount,
                                                                                ServerWebExchange exchange
    ) {
        return paymentMethodService.retrievePaymentMethods(amount != null ? amount.intValue() : null)
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
                                                                           ServerWebExchange exchange
    ) {
        return paymentMethodService.retrievePaymentMethodById(id)
                .map(this::paymentMethodToResponse);
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodResponseDto>> newPaymentMethod(
                                                                           @Valid Mono<PaymentMethodRequestDto> paymentMethodRequestDto,
                                                                           ServerWebExchange exchange
    ) {
        return paymentMethodRequestDto.flatMap(
                request -> paymentMethodService.createPaymentMethod(
                        request.getName(),
                        request.getDescription(),
                        request.getRanges().stream()
                                .map(r -> Pair.of(r.getMin(), r.getMax()))
                                .toList(),
                        request.getPaymentTypeCode(),
                        request.getAsset()
                )
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
        return response;
    }

    @Override
    public Mono<ResponseEntity<CalculateFeeResponseDto>> calculateFees(
                                                                       String id,
                                                                       Mono<CalculateFeeRequestDto> calculateFeeRequestDto,
                                                                       Integer maxOccurrences,
                                                                       ServerWebExchange exchange
    ) {

        return paymentMethodService.computeFee(calculateFeeRequestDto, id, maxOccurrences).map(
                ResponseEntity::ok
        );
    }

    @Override
    public Mono<ResponseEntity<CreateSessionResponseDto>> createSession(
                                                                        String id,
                                                                        ServerWebExchange exchange
    ) {
        return paymentMethodService.createSessionForPaymentMethod(id).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SessionPaymentMethodResponseDto>> getSessionPaymentMethod(
                                                                                         String id,
                                                                                         String sessionId,
                                                                                         ServerWebExchange exchange
    ) {
        log.info("[Payment Method controller] Retrieve card data from NPG");
        return paymentMethodService.getCardDataInformation(id, URLEncoder.encode(sessionId, Charset.defaultCharset()))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> validateSession(
                                                      String id,
                                                      String sessionId,
                                                      Mono<SessionValidateRequestDto> sessionValidateRequestDto,
                                                      ServerWebExchange exchange
    ) {
        return sessionValidateRequestDto
                .map(SessionValidateRequestDto::getSecurityToken)
                .map(securityToken -> paymentMethodService.isSessionValid(sessionId, securityToken))
                .flatMap(isValid -> isValid.map(Mono::just).orElseGet(Mono::empty))
                .filter(Boolean::booleanValue)
                .map(_unused -> ResponseEntity.noContent().<Void>build())
                .switchIfEmpty(Mono.error(new SessionIdNotFoundException(sessionId)));
    }
}
