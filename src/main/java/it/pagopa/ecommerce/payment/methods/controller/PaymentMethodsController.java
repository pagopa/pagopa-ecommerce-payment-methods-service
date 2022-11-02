package it.pagopa.ecommerce.payment.methods.controller;

import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.application.PspService;
import it.pagopa.ecommerce.payment.methods.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodAlreadyInUseException;
import it.pagopa.ecommerce.payment.methods.exception.PspAlreadyInUseException;
import it.pagopa.ecommerce.payment.methods.server.api.PaymentMethodsApi;
import it.pagopa.ecommerce.payment.methods.server.model.PSPsResponseDto;
import it.pagopa.ecommerce.payment.methods.server.model.PatchPaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodResponseDto;
import it.pagopa.ecommerce.payment.methods.server.model.RangeDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ProblemJsonDto;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class PaymentMethodsController implements PaymentMethodsApi {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private PspService pspService;

    @Autowired
    private ApiConfigClient apiConfigClient;

    @ExceptionHandler({
            PaymentMethodAlreadyInUseException.class,
            PspAlreadyInUseException.class
    })
    private ResponseEntity<ProblemJsonDto> errorHandler(RuntimeException exception) {
        if(exception instanceof PaymentMethodAlreadyInUseException){
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title("Bad request").detail("Payment method already in use"), HttpStatus.BAD_REQUEST);
        } else if (exception instanceof PspAlreadyInUseException) {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(404).title("Bad request").detail("PSP already in use"), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(
                    new ProblemJsonDto().status(500).title("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Mono<ResponseEntity<Flux<PaymentMethodResponseDto>>> getAllPaymentMethods(BigDecimal amount, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(paymentMethodService.retrievePaymentMethods(amount != null ? amount.intValue() : null)
                .map(paymentMethod -> {
                    PaymentMethodResponseDto responseDto = new PaymentMethodResponseDto();
                    responseDto.setId(paymentMethod.getPaymentMethodID().value().toString());
                    responseDto.setName(paymentMethod.getPaymentMethodName().value());
                    responseDto.setDescription(paymentMethod.getPaymentMethodDescription().value());
                    responseDto.setStatus(PaymentMethodResponseDto.StatusEnum.valueOf(paymentMethod.getPaymentMethodStatus().value().toString()));
                    responseDto.setRanges(paymentMethod.getPaymentMethodRanges().stream().map(
                            r -> {
                                RangeDto rangeDto = new RangeDto();
                                rangeDto.setMin(r.min());
                                rangeDto.setMax(r.max());
                                return rangeDto;
                            }
                    ).collect(Collectors.toList()));
                    responseDto.setPaymentTypeCode(paymentMethod.getPaymentMethodTypeCode().value());

                    return responseDto;
                })
        ));
    }


    @Override
    public Mono<ResponseEntity<PSPsResponseDto>> getPSPs(Integer amount, String lang, String paymentTypeCode, ServerWebExchange exchange) {
        return pspService.retrievePsps(amount, lang, paymentTypeCode).collectList().flatMap(
                pspDtos -> {
                    PSPsResponseDto responseDto = new PSPsResponseDto();
                    responseDto.setPsp(pspDtos);

                    return Mono.just(ResponseEntity.ok(responseDto));
                }
        );
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodResponseDto>> getPaymentMethod(String id, ServerWebExchange exchange) {
        return paymentMethodService.retrievePaymentMethodById(id)
                .map(this::paymentMethodToResponse);
    }

    @Override
    public Mono<ResponseEntity<PSPsResponseDto>> getPaymentMethodsPSPs(String id, Integer amount, String lang, ServerWebExchange exchange) {
        return paymentMethodService.retrievePaymentMethodById(id)
                .flatMap(pm -> pspService.retrievePsps(amount, lang, pm.getPaymentMethodTypeCode().value()).collectList().flatMap(pspDtos -> {
                        PSPsResponseDto responseDto = new PSPsResponseDto();
                        responseDto.setPsp(pspDtos);

                        return Mono.just(ResponseEntity.ok(responseDto));
                    })
                );
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodResponseDto>> newPaymentMethod(@Valid Mono<PaymentMethodRequestDto> paymentMethodRequestDto,
                                                                           ServerWebExchange exchange) {
        return paymentMethodRequestDto.flatMap(request -> paymentMethodService.createPaymentMethod(
                request.getName(),
                request.getDescription(),
                request.getRanges().stream().map(r -> Pair.of(r.getMin(), r.getMax())).toList(),
                request.getPaymentTypeCode(), request.getAsset())
                .map(this::paymentMethodToResponse)
        );
    }

    @Override
    public Mono<ResponseEntity<PaymentMethodResponseDto>> patchPaymentMethod(String id, Mono<PatchPaymentMethodRequestDto> patchPaymentMethodRequestDto, ServerWebExchange exchange) {
        return patchPaymentMethodRequestDto
                .flatMap(request -> paymentMethodService
                        .updatePaymentMethodStatus(id, PaymentMethodStatusEnum.valueOf(request.getStatus().toString()))
                        .map(this::paymentMethodToResponse));
    }

    @Override
    public Mono<ResponseEntity<Void>> scheduleUpdatePSPs(ServerWebExchange exchange) {
        AtomicReference<Integer> currentPage = new AtomicReference<>(0);

        return apiConfigClient.getPSPs(0, 50, null).expand(
                servicesDto -> {
                    if (servicesDto.getPageInfo().getTotalPages().equals(currentPage.get()+1)) {
                        return Mono.empty();
                    }
                    return apiConfigClient.getPSPs(currentPage.updateAndGet(v -> v + 1), 50, null);
                }
        ).collectList().map(
                services -> {
                    ServicesDto servicesDto = new ServicesDto();

                    for (ServicesDto service : services) {
                        servicesDto.setServices(Stream.concat(servicesDto.getServices().stream(),
                                service.getServices().stream()).toList());
                    }

                    pspService.updatePSPs(servicesDto);
                    paymentMethodService.updatePaymentMethodRanges(servicesDto);

                    return ResponseEntity.accepted().build();
                }
        );
    }

    private ResponseEntity<PaymentMethodResponseDto> paymentMethodToResponse(PaymentMethod paymentMethod){
        PaymentMethodResponseDto response = new PaymentMethodResponseDto();
        response.setId(paymentMethod.getPaymentMethodID().value().toString());
        response.setName(paymentMethod.getPaymentMethodName().value());
        response.setDescription(paymentMethod.getPaymentMethodDescription().value());
        response.setStatus(PaymentMethodResponseDto.StatusEnum.valueOf(
                paymentMethod.getPaymentMethodStatus().value().toString()));
        response.setRanges(paymentMethod.getPaymentMethodRanges().stream().map(
                r -> {
                    RangeDto rangeDto = new RangeDto();
                    rangeDto.setMin(r.min());
                    rangeDto.setMax(r.max());
                    return rangeDto;
                }
        ).collect(Collectors.toList()));
        response.setPaymentTypeCode(paymentMethod.getPaymentMethodTypeCode().value());
        return ResponseEntity.ok(response);
    }
}
