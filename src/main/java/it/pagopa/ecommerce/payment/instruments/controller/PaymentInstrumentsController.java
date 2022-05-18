package it.pagopa.ecommerce.payment.instruments.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentService;
import it.pagopa.ecommerce.payment.instruments.server.api.PaymentInstrumentsApi;
import it.pagopa.ecommerce.payment.instruments.server.model.PatchPaymentInstrumentRequestDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentInstrumentRequestDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentInstrumentResponseDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentInstrumentResponseDto.StatusEnum;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PaymentInstrumentsController implements PaymentInstrumentsApi {

    @Autowired
    private PaymentInstrumentService paymentInstrumentService;

    @Override
    public Mono<ResponseEntity<PaymentInstrumentResponseDto>> newPaymentInstrument(
            @Valid Mono<PaymentInstrumentRequestDto> paymentInstrumentRequestDto, ServerWebExchange exchange) {

        return paymentInstrumentRequestDto.flatMap(request -> paymentInstrumentService.createPaymentInstrument(
                request.getName(),
                request.getDescription())).map(paymentInstrument -> {
                    PaymentInstrumentResponseDto response = new PaymentInstrumentResponseDto();
                    response.setId(paymentInstrument.getPaymentInstrumentID().value().toString());
                    response.setName(paymentInstrument.getPaymentInstrumentName().value());
                    response.setDescription(paymentInstrument.getPaymentInstrumentDescription().value());
                    response.setStatus(
                            StatusEnum.valueOf(paymentInstrument.getPaymentInstrumentStatus().value().toString()));
                    return ResponseEntity.ok(response);
                });
    }

    @Override
    public Mono<ResponseEntity<Flux<PaymentInstrumentResponseDto>>> getAllPaymentInstruments(
            ServerWebExchange exchange) {

        return Mono.just(ResponseEntity.ok(paymentInstrumentService.retrivePaymentInstruments()
                .map(paymentInstrument -> {
                    PaymentInstrumentResponseDto response = new PaymentInstrumentResponseDto();
                    response.setId(paymentInstrument.getPaymentInstrumentID().value().toString());
                    response.setName(paymentInstrument.getPaymentInstrumentName().value());
                    response.setDescription(paymentInstrument.getPaymentInstrumentDescription().value());
                    response.setStatus(
                            StatusEnum.valueOf(paymentInstrument.getPaymentInstrumentStatus().value().toString()));
                    return response;
                })));
    }

    @Override
    public Mono<ResponseEntity<PaymentInstrumentResponseDto>> getPaymentInstrument(String id,
            ServerWebExchange exchange) {
        return paymentInstrumentService.retrivePaymentInstrumentById(id)
                .map(paymentInstrument -> {
                    PaymentInstrumentResponseDto response = new PaymentInstrumentResponseDto();
                    response.setId(paymentInstrument.getPaymentInstrumentID().value().toString());
                    response.setName(paymentInstrument.getPaymentInstrumentName().value());
                    response.setDescription(paymentInstrument.getPaymentInstrumentDescription().value());
                    response.setStatus(
                            StatusEnum.valueOf(paymentInstrument.getPaymentInstrumentStatus().value().toString()));
                    return ResponseEntity.ok(response);
                });
    }

    @Override
    public Mono<ResponseEntity<PaymentInstrumentResponseDto>> patchPaymentInstrument(String id,
            @Valid Mono<PatchPaymentInstrumentRequestDto> paymentInstrumentRequestDto, ServerWebExchange exchange) {
        return paymentInstrumentRequestDto
                .flatMap(request -> paymentInstrumentService
                        .patchPaymentInstrument(id, PaymentInstrumentStatusEnum.valueOf(request.getStatus().getValue()))
                        .map(paymentInstrument -> {
                            PaymentInstrumentResponseDto response = new PaymentInstrumentResponseDto();
                            response.setId(paymentInstrument.getPaymentInstrumentID().value().toString());
                            response.setName(paymentInstrument.getPaymentInstrumentName().value());
                            response.setDescription(paymentInstrument.getPaymentInstrumentDescription().value());
                            response.setStatus(
                                    StatusEnum.valueOf(
                                            paymentInstrument.getPaymentInstrumentStatus().value().toString()));
                            return ResponseEntity.ok(response);
                        }));
    }

}
