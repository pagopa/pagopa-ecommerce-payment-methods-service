package it.pagopa.ecommerce.payment.instruments.application;

import java.util.UUID;
import java.util.stream.Collectors;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrument;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentFactory;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentRepository;
import it.pagopa.ecommerce.payment.instruments.utils.ApplicationService;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@ApplicationService
@Slf4j
public class PaymentInstrumentService {

    @Autowired
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @Autowired
    private PaymentInstrumentFactory paymentInstrumentFactory;

    public Mono<PaymentInstrument> createPaymentInstrument(String paymentInstrumentName,
                                                           String paymentInstrumentDescription,
                                                           String paymentInstrumentCategoryId,
                                                           String paymentInstrumentTypeCode) {

        log.debug("[Payment instrument Aggregate] Create new Aggregate");
        Mono<PaymentInstrument> paymentInstrument = paymentInstrumentFactory.newPaymentInstrument(
                new PaymentInstrumentID(UUID.randomUUID()),
                new PaymentInstrumentName(paymentInstrumentName),
                new PaymentInstrumentDescription(paymentInstrumentDescription),
                new PaymentInstrumentStatus(PaymentInstrumentStatusEnum.ENABLED),
                new PaymentInstrumentCategoryID(UUID.fromString(paymentInstrumentCategoryId)),
                new PaymentInstrumentType(paymentInstrumentTypeCode));

        // TODO create converter aggregate - document
        log.debug("[Payment instrument Aggregate] Store Aggregate");
        return paymentInstrument.flatMap(
                        p -> paymentInstrumentRepository
                                .save(new PaymentInstrumentDocument(
                                        p.getPaymentInstrumentID().value().toString(),
                                        p.getPaymentInstrumentName().value(),
                                        p.getPaymentInstrumentDescription().value(),
                                        p.getPaymentInstrumentStatus().value().toString(),
                                        p.getPaymentInstrumentCategoryID().value().toString(),
                                        p.getPaymentInstrumentCategoryName().value(),
                                        p.getPaymentInstrumentCategoryTypes().stream().map(PaymentInstrumentType::value)
                                                .collect(Collectors.toList()),
                                        p.getPaymentInstrumentTypeCode().value()))
                .map(document -> new PaymentInstrument(
                        new PaymentInstrumentID(
                                UUID.fromString(document.getPaymentInstrumentID())),
                        new PaymentInstrumentName(document.getPaymentInstrumentName()),
                        new PaymentInstrumentDescription(
                                document.getPaymentInstrumentDescription()),
                        new PaymentInstrumentStatus(PaymentInstrumentStatusEnum
                                .valueOf(document.getPaymentInstrumentStatus())),
                        new PaymentInstrumentCategoryID(UUID.fromString(document.getPaymentInstrumentCategoryID())),
                        new PaymentInstrumentCategoryName(document.getPaymentInstrumentCategoryName()),
                        document.getPaymentInstrumentCategoryTypes().stream().map(
                                PaymentInstrumentType::new
                        ).collect(Collectors.toList()),
                        new PaymentInstrumentType(document.getPaymentInstrumentTypeCode()))
                ));
    }

    public Flux<PaymentInstrument> retrivePaymentInstruments(String categoryId) {

        log.debug("[Payment instrument Aggregate] Retrive Aggregate");
        Flux<PaymentInstrumentDocument> docs;

        log.info("Category id: {}", categoryId);
        if(categoryId == null || categoryId.isEmpty() || categoryId.isBlank()) {
            docs = paymentInstrumentRepository.findAll();
        } else {
            docs = paymentInstrumentRepository.findByPaymentInstrumentCategoryID(categoryId);
        }

        return docs.map(document -> new PaymentInstrument(
                new PaymentInstrumentID(
                        UUID.fromString(document.getPaymentInstrumentID())),
                new PaymentInstrumentName(document.getPaymentInstrumentName()),
                new PaymentInstrumentDescription(
                        document.getPaymentInstrumentDescription()),
                new PaymentInstrumentStatus(PaymentInstrumentStatusEnum
                        .valueOf(document.getPaymentInstrumentStatus())),
                new PaymentInstrumentCategoryID(UUID.fromString(document.getPaymentInstrumentCategoryID())),
                new PaymentInstrumentCategoryName(document.getPaymentInstrumentCategoryName()),
                document.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentInstrumentType::new).collect(Collectors.toList()),
                new PaymentInstrumentType(document.getPaymentInstrumentTypeCode())));
    }

    public Mono<PaymentInstrument> patchPaymentInstrument(String id,
                                                          PaymentInstrumentStatusEnum enable) {

        log.debug("[Payment instrument Aggregate] Patch Aggregate");

        return paymentInstrumentRepository
                .findById(id)
                .map(document -> {
                    PaymentInstrument paymentInstrument = new PaymentInstrument(
                            new PaymentInstrumentID(
                                    UUID.fromString(document
                                            .getPaymentInstrumentID())),
                            new PaymentInstrumentName(document.getPaymentInstrumentName()),
                            new PaymentInstrumentDescription(
                                    document.getPaymentInstrumentDescription()),
                            new PaymentInstrumentStatus(PaymentInstrumentStatusEnum
                                    .valueOf(document.getPaymentInstrumentStatus())),
                            new PaymentInstrumentCategoryID(UUID.fromString(document.getPaymentInstrumentCategoryID())),
                            new PaymentInstrumentCategoryName(document.getPaymentInstrumentCategoryName()),
                            document.getPaymentInstrumentCategoryTypes().stream()
                                    .map(PaymentInstrumentType::new).collect(Collectors.toList()),
                            new PaymentInstrumentType(document.getPaymentInstrumentTypeCode()));
                    paymentInstrument.enablePaymentInstrument(new PaymentInstrumentStatus(enable));
                    return paymentInstrument;
                }).flatMap(
                        p -> paymentInstrumentRepository
                                .save(new PaymentInstrumentDocument(
                                        p.getPaymentInstrumentID().value().toString(),
                                        p.getPaymentInstrumentName().value(),
                                        p.getPaymentInstrumentDescription().value(),
                                        p.getPaymentInstrumentStatus().value().toString(),
                                        p.getPaymentInstrumentCategoryID().value().toString(),
                                        p.getPaymentInstrumentCategoryName().value(),
                                        p.getPaymentInstrumentCategoryTypes().stream().map(PaymentInstrumentType::value)
                                                .collect(Collectors.toList()),
                                        p.getPaymentInstrumentTypeCode().value()))
                )
                .map(document -> {
                    PaymentInstrument paymentInstrument = new PaymentInstrument(
                            new PaymentInstrumentID(
                                    UUID.fromString(document
                                            .getPaymentInstrumentID())),
                            new PaymentInstrumentName(document.getPaymentInstrumentName()),
                            new PaymentInstrumentDescription(
                                    document.getPaymentInstrumentDescription()),
                            new PaymentInstrumentStatus(PaymentInstrumentStatusEnum
                                    .valueOf(document.getPaymentInstrumentStatus())),
                            new PaymentInstrumentCategoryID(UUID.fromString(document.getPaymentInstrumentCategoryID())),
                            new PaymentInstrumentCategoryName(document.getPaymentInstrumentName()),
                            document.getPaymentInstrumentCategoryTypes().stream().map(
                                    PaymentInstrumentType::new
                            ).collect(Collectors.toList()),
                            new PaymentInstrumentType(document.getPaymentInstrumentTypeCode())
                    );

                    paymentInstrument.enablePaymentInstrument(new PaymentInstrumentStatus(enable));
                    return paymentInstrument;
                });
    }

    public Mono<PaymentInstrument> retrivePaymentInstrumentById(String id) {

        log.debug("[Payment instrument Aggregate] Retrive Aggregate");

        return paymentInstrumentRepository
                .findById(id)
                .map(document -> new PaymentInstrument(
                        new PaymentInstrumentID(
                                UUID.fromString(document.getPaymentInstrumentID())),
                        new PaymentInstrumentName(document.getPaymentInstrumentName()),
                        new PaymentInstrumentDescription(
                                document.getPaymentInstrumentDescription()),
                        new PaymentInstrumentStatus(PaymentInstrumentStatusEnum
                                .valueOf(document.getPaymentInstrumentStatus())),
                        new PaymentInstrumentCategoryID(UUID.fromString(document.getPaymentInstrumentCategoryID())),
                        new PaymentInstrumentCategoryName(document.getPaymentInstrumentCategoryName()),
                        document.getPaymentInstrumentCategoryTypes().stream()
                                .map(PaymentInstrumentType::new).collect(Collectors.toList()),
                        new PaymentInstrumentType(document.getPaymentInstrumentTypeCode())));
    }
}
