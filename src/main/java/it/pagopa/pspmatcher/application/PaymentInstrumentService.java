package it.pagopa.pspmatcher.application;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.pagopa.pspmatcher.domain.aggregates.PaymentInstrument;
import it.pagopa.pspmatcher.domain.aggregates.PaymentInstrumentFactory;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentDescription;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentEnabled;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentID;
import it.pagopa.pspmatcher.domain.valueobjects.PaymentInstrumentName;
import it.pagopa.pspmatcher.infrastructure.PaymentInstrumentDocument;
import it.pagopa.pspmatcher.infrastructure.PaymentInstrumentRepository;
import it.pagopa.pspmatcher.utils.ApplicationService;
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
            String paymentInstrumentDescription) {

        log.debug("[Payment instrument Aggregate] Create new Aggregate");
        Mono<PaymentInstrument> paymentInstrument = paymentInstrumentFactory.newPaymentInstrument(
                new PaymentInstrumentID(UUID.randomUUID()),
                new PaymentInstrumentName(paymentInstrumentName),
                new PaymentInstrumentDescription(paymentInstrumentDescription),
                new PaymentInstrumentEnabled(Boolean.TRUE));

        // TODO create converter aggregate - document
        log.debug("[Payment instrument Aggregate] Store Aggregate");
        return paymentInstrument.flatMap(
                p -> paymentInstrumentRepository
                        .save(new PaymentInstrumentDocument(
                                p.getPaymentInstrumentID().value().toString(),
                                p.getPaymentInstrumentName().value(),
                                p.getPaymentInstrumentDescription().value(),
                                p.getPsp(),
                                p.getPaymentInstrumentEnabled().value())))
                .map(document -> new PaymentInstrument(
                        new PaymentInstrumentID(
                                UUID.fromString(document.getPaymentInstrumentID())),
                        new PaymentInstrumentName(document.getPaymentInstrumentName()),
                        new PaymentInstrumentDescription(
                                document.getPaymentInstrumentDescription()),
                        new PaymentInstrumentEnabled(document.getPaymentInstrumentEnabled())));
    }

    public Flux<PaymentInstrument> retrivePaymentInstruments() {

        // TODO create converter aggregate - document
        log.debug("[Payment instrument Aggregate] Retrive Aggregate");
        return paymentInstrumentRepository
                .findAll()
                .map(document -> new PaymentInstrument(
                        new PaymentInstrumentID(
                                UUID.fromString(document.getPaymentInstrumentID())),
                        new PaymentInstrumentName(document.getPaymentInstrumentName()),
                        new PaymentInstrumentDescription(
                                document.getPaymentInstrumentDescription()),
                        new PaymentInstrumentEnabled(document.getPaymentInstrumentEnabled())));
    }

    public Mono<PaymentInstrument> patchPaymentInstrument(String id,
            Boolean enable) {

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
                            new PaymentInstrumentEnabled(
                                    document.getPaymentInstrumentEnabled()));
                    paymentInstrument.enablePaymentInstrument(new PaymentInstrumentEnabled(enable));
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
                        new PaymentInstrumentEnabled(document.getPaymentInstrumentEnabled())));
    }
}
