package it.pagopa.ecommerce.payment.instruments.application;

import it.pagopa.ecommerce.payment.instruments.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.Psp;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PspFactory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.instruments.server.model.PspDto;
import it.pagopa.ecommerce.payment.instruments.utils.ApplicationService;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.EntityResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ApplicationService
@Slf4j
public class PspService {

    @Autowired
    private PspRepository pspRepository;

    @Autowired
    private PspFactory pspFactory;

    @Autowired
    private ApiConfigClient apiConfigClient;

    public void updatePSPs(ServicesDto servicesDto) {
        servicesDto.getServices().forEach(service -> {
            Mono<Psp> pspMono = pspFactory.newPsp(
                    new PspCode(service.getPspCode()), new PaymentInstrumentID(UUID.randomUUID()),
                    new PspStatus(PaymentInstrumentStatusEnum.ENABLED),
                    new PspBusinessName(service.getPspBusinessName()),
                    new PspBrokerName(service.getBrokerPspCode()),
                    new PspDescription(service.getServiceDescription()),
                    new PspPaymentInstrumentType(service.getPaymentTypeCode())
            );

            pspMono.flatMap(
                    p ->
                            pspRepository.save(
                                    new PspDocument(
                                            p.getPspCode().value(),
                                            p.getPaymentInstrumentID().value().toString(),
                                            p.getPspStatus().value().getCode(),
                                            p.getPspPaymentInstrumentType().value(),
                                            p.getPspBusinessName().value(),
                                            p.getPspBrokerName().value(),
                                            p.getPspDescription().value(),
                                            new ArrayList<>(),
                                            new ArrayList<>()
                                    )
                            ).map(doc -> {
                                log.info("[Psp Service] {} added to db", doc.getPspName());
                                return doc;
                            })
            ).subscribe();
        });
    }

    public Flux<PspDto> retrivePsps() {

        log.debug("[Payment instrument Aggregate] Retrive Aggregate");
        return pspRepository
                .findAll()
                .map(doc -> {
                    PspDto pspDto = new PspDto();

                    pspDto.setDescription(doc.getPspDescription());
                    pspDto.setName(doc.getPspName());
                    pspDto.setStatus(PspDto.StatusEnum.fromValue(doc.getPspStatus()));
                    pspDto.setBrokerName(doc.getPspBrokerName());
                    pspDto.setCode(doc.getPspCode());
                    pspDto.setLanguages(doc.getPspLanguages().stream()
                            .map(PspDto.LanguagesEnum::valueOf).collect(Collectors.toList()));
                    // TODO: add ranges pspDto.setRanges();
                    pspDto.setType(doc.getPspType());
                    pspDto.setPaymentInstrumentID(doc.getPaymentInstrumentID());

                    return pspDto;
                });
    }
}

