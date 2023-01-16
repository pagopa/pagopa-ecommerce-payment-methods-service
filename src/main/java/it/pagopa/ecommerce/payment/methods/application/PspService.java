package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.payment.methods.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.Psp;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PspFactory;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspAmount;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspBrokerName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspBusinessName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspChannelCode;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspCode;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspFee;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspLanguage;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspPaymentMethodType;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspStatus;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocumentKey;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.methods.infrastructure.rule.FilterRuleEngine;
import it.pagopa.ecommerce.payment.methods.server.model.PspDto;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.ecommerce.payment.methods.utils.LanguageEnum;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;

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
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private FilterRuleEngine filterRuleEngine;

    public void updatePSPs(ServicesDto servicesDto) {
        servicesDto.getServices().forEach(service -> {

            BigInteger min = BigDecimal.valueOf(service.getMinimumAmount()).multiply(BigDecimal.valueOf(100))
                    .toBigInteger();
            BigInteger max = BigDecimal.valueOf(service.getMaximumAmount()).multiply(BigDecimal.valueOf(100))
                    .toBigInteger();
            BigInteger fee = BigDecimal.valueOf(service.getFixedCost()).multiply(BigDecimal.valueOf(100))
                    .toBigInteger();

            Mono<Psp> pspMono = pspFactory.newPsp(
                    new PspCode(service.getPspCode()),
                    new PspPaymentMethodType(service.getPaymentTypeCode()),
                    new PspStatus(PaymentMethodStatusEnum.ENABLED),
                    new PspBusinessName(service.getPspBusinessName()),
                    new PspBrokerName(service.getBrokerPspCode()),
                    new PspDescription(service.getServiceDescription()),
                    new PspLanguage(LanguageEnum.valueOf(service.getLanguageCode().getValue())),
                    new PspAmount(min),
                    new PspAmount(max),
                    new PspChannelCode(service.getChannelCode()),
                    new PspFee(fee)
            );

            pspMono.flatMap(
                    p -> pspRepository.save(
                            new PspDocument(
                                    new PspDocumentKey(
                                            p.getPspCode().value(),
                                            p.getPspPaymentMethodType().value(),
                                            p.getPspChannelCode().value(),
                                            p.getPspLanguage().value().getLanguage()
                                    ),
                                    p.getPspStatus().value().getCode(),
                                    p.getPspBusinessName().value(),
                                    p.getPspBrokerName().value(),
                                    p.getPspDescription().value(),
                                    p.getPspMinAmount().value().longValue(),
                                    p.getPspMaxAmount().value().longValue(),
                                    p.getPspFixedCost().value().longValue()
                            )
                    ).map(doc -> {
                        log.debug("[Psp Service] {} added to db", doc.getPspBusinessName());
                        return doc;
                    })
            ).subscribe();
        });
    }

    public Flux<PspDto> retrievePsps(
                                     Integer amount,
                                     String language,
                                     String paymentTypeCode
    ) {

        log.debug("[Payment Method Aggregate] Retrive Aggregate");

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

    public Flux<PspDocument> getPspByFilter(
                                            Integer amount,
                                            String language,
                                            String paymentTypeCode
    ) {
        language = language == null ? null : language.toUpperCase();
        paymentTypeCode = paymentTypeCode == null ? null : paymentTypeCode.toUpperCase();

        return filterRuleEngine.applyFilter(amount, language, paymentTypeCode);
    }
}
