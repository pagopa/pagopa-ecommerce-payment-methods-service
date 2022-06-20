package it.pagopa.ecommerce.payment.instruments.controller;

import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentService;
import it.pagopa.ecommerce.payment.instruments.application.PspService;
import it.pagopa.ecommerce.payment.instruments.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.instruments.server.api.PaymentInstrumentsApi;
import it.pagopa.ecommerce.payment.instruments.server.model.PSPsResponseDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PatchPaymentInstrumentRequestDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentInstrumentRequestDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentInstrumentResponseDto;
import it.pagopa.ecommerce.payment.instruments.server.model.PaymentInstrumentResponseDto.StatusEnum;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class PaymentInstrumentsController implements PaymentInstrumentsApi {

    @Autowired
    private PaymentInstrumentService paymentInstrumentService;

    @Autowired
    private PspService pspService;

    @Autowired
    private ApiConfigClient apiConfigClient;

    @Override
    public Mono<ResponseEntity<PaymentInstrumentResponseDto>> newPaymentInstrument(
            @Valid Mono<PaymentInstrumentRequestDto> paymentInstrumentRequestDto, ServerWebExchange exchange) {

        return paymentInstrumentRequestDto.flatMap(request -> paymentInstrumentService.createPaymentInstrument(
                request.getName(),
                request.getDescription(),
                request.getType())).map(paymentInstrument -> {
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
    public Mono<ResponseEntity<PSPsResponseDto>> getPSPs(Integer amount, String lang, ServerWebExchange exchange) {
        return pspService.retrievePsps(amount, lang).collectList().flatMap(pspDtos -> {
            PSPsResponseDto responseDto = new PSPsResponseDto();
            responseDto.setPsp(pspDtos);

            return Mono.just(ResponseEntity.ok(responseDto));
        });
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


    @Override
    public Mono<ResponseEntity<Void>> scheduleUpdatePSPs(ServerWebExchange exchange) {
        AtomicReference<Integer> currentPage = new AtomicReference<>(0);

        return apiConfigClient.getPSPs(0, 50, null).expand(
                servicesDto -> {
                    if (servicesDto.getPageInfo().getTotalPages().equals(currentPage.get())) {
                        return Mono.empty();
                    }
                    return apiConfigClient.getPSPs(currentPage.updateAndGet(v -> v + 1), 50, null);
                }
        ).collectList().map(
                services -> {
                    for (ServicesDto service: services){
                        pspService.updatePSPs(service);
                    }

                    return ResponseEntity.accepted().build();
                }
        );
    }
}
