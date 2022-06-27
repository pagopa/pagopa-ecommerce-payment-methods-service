package it.pagopa.ecommerce.payment.instruments.application;

import it.pagopa.ecommerce.payment.instruments.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.Psp;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PspFactory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocumentKey;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.instruments.infrastructure.rule.FilterRuleEngine;
import it.pagopa.ecommerce.payment.instruments.server.model.PspDto;
import it.pagopa.ecommerce.payment.instruments.utils.ApplicationService;
import it.pagopa.ecommerce.payment.instruments.utils.LanguageEnum;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Autowired
    private FilterRuleEngine filterRuleEngine;

    public void updatePSPs(ServicesDto servicesDto) {
        servicesDto.getServices().forEach(service -> {
            Mono<Psp> pspMono = pspFactory.newPsp(
                    new PspCode(service.getPspCode()),
                    new PspPaymentInstrumentType(service.getPaymentTypeCode()),
                    new PspStatus(PaymentInstrumentStatusEnum.ENABLED),
                    new PspBusinessName(service.getPspBusinessName()),
                    new PspBrokerName(service.getBrokerPspCode()),
                    new PspDescription(service.getServiceDescription()),
                    new PspLanguage(LanguageEnum.valueOf(service.getLanguageCode().getValue())),
                    new PspAmount(service.getMinimumAmount()),
                    new PspAmount(service.getMaximumAmount()),
                    new PspChannelCode(service.getChannelCode()),
                    new PspFee(service.getFixedCost()));

            pspMono.flatMap(
                    p ->
                            pspRepository.save(
                                    new PspDocument(
                                            new PspDocumentKey(p.getPspCode().value(),
                                                    p.getPspPaymentInstrumentType().value(),
                                                    p.getPspChannelCode().value(),
                                                    p.getPspLanguage().value().getLanguage()),
                                            p.getPspStatus().value().getCode(),
                                            p.getPspBusinessName().value(),
                                            p.getPspBrokerName().value(),
                                            p.getPspDescription().value(),
                                            p.getPspMinAmount().value(),
                                            p.getPspMaxAmount().value(),
                                            p.getPspFixedCost().value()
                                    )
                            ).map(doc -> {
                                log.debug("[Psp Service] {} added to db", doc.getPspBusinessName());
                                return doc;
                            })
            ).subscribe();
        });
    }

    public Flux<PspDto> retrievePsps(Integer amount, String language, String paymentTypeCode) {

        log.debug("[Payment instrument Aggregate] Retrive Aggregate");

        return getPspByFilter(amount, language, paymentTypeCode).map(doc -> {
            PspDto pspDto = new PspDto();

            pspDto.setCode(doc.getPspDocumentKey().getPspCode());
            pspDto.setPaymentTypeCode(doc.getPspDocumentKey().getPspPaymentTypeCode());
            pspDto.setChannelCode(doc.getPspDocumentKey().getPspChannelCode());
            pspDto.setDescription(doc.getPspDescription());
            pspDto.setBusinessName(doc.getPspBusinessName());
            pspDto.setStatus(PspDto.StatusEnum.fromValue(doc.getPspStatus()));
            pspDto.setBrokerName(doc.getPspBrokerName());
            pspDto.setLanguage(PspDto.LanguageEnum.fromValue(doc.getPspDocumentKey().getPspLanguageCode()));
            pspDto.setMinAmount(doc.getPspMinAmount());
            pspDto.setMaxAmount(doc.getPspMaxAmount());
            pspDto.setFixedCost(doc.getPspFixedCost());

            return pspDto;
        });
    }

    public Flux<PspDocument> getPspByFilter(Integer amount, String language, String paymentTypeCode) {
        language = language == null ? null : language.toUpperCase();
        paymentTypeCode = paymentTypeCode == null ? null : paymentTypeCode.toUpperCase();

        return filterRuleEngine.applyFilter(amount, language, paymentTypeCode);
    }
}

