package it.pagopa.ecommerce.payment.instruments.application;

import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.instruments.utils.ApplicationService;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentMethodStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ApplicationService
@Slf4j
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private PaymentMethodFactory paymentInstrumentFactory;

    public Mono<PaymentMethod> createPaymentMethod(String paymentMethodName,
                                                   String paymentMethodDescription,
                                                   List<Pair<Integer, Integer>> ranges,
                                                   String paymentMethodTypeCode){
        log.debug("[Payment Method Aggregate] Create new aggregate");
        Mono<PaymentMethod> paymentMethod = paymentInstrumentFactory.newPaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName(paymentMethodName),
                new PaymentMethodDescription(paymentMethodDescription),
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                ranges.stream().map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond())).collect(Collectors.toList()),
                new PaymentMethodType(paymentMethodTypeCode)
        );

        log.debug("[Payment Method Aggregate] Store new aggregate");

        return paymentMethod.flatMap(p -> paymentMethodRepository.save(
                new PaymentMethodDocument(p.getPaymentMethodID().toString(),
                        p.getPaymentMethodName().value(),
                        p.getPaymentMethodDescription().value(),
                        p.getPaymentMethodStatus().value().toString(),
                        p.getPaymentMethodRanges().stream().map(r -> Pair.of(r.min(), r.max())).collect(Collectors.toList()),
                        p.getPaymentMethodTypeCode().value())
        ).map(doc -> new PaymentMethod(
                new PaymentMethodID(UUID.fromString(doc.getPaymentMethodID())),
                new PaymentMethodName(doc.getPaymentMethodName()),
                new PaymentMethodDescription(doc.getPaymentMethodDescription()),
                new PaymentMethodStatus(PaymentMethodStatusEnum.valueOf(doc.getPaymentMethodStatus())),
                doc.getPaymentMethodRanges().stream().map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond())).collect(Collectors.toList()),
                new PaymentMethodType(doc.getPaymentMethodTypeCode())
        )));
    }

    public Flux<PaymentMethod> retrievePaymentMethods(Integer amount) {
        log.debug("[Payment Method Aggregate] Retrieve Aggregate");

        return paymentMethodRepository.findAll().map(this::docToAggregate);
    }

    public Mono<PaymentMethod> updatePaymentMethodStatus(String id,
                                                  PaymentMethodStatusEnum status){
        log.debug("[Payment instrument Aggregate] Patch Aggregate");

        return paymentMethodRepository
                .findById(id)
                // TODO: add error on invalid ID
                // .switchIfEmpty()
                .map(this::docToAggregate)
                .map(p -> {
                    p.setPaymentMethodStatus(status);
                    return p;
                })
                .flatMap(
                        p -> paymentMethodRepository
                                .save(new PaymentMethodDocument(
                                        p.getPaymentMethodID().value().toString(),
                                        p.getPaymentMethodName().value(),
                                        p.getPaymentMethodDescription().value(),
                                        p.getPaymentMethodStatus().value().toString(),
                                        p.getPaymentMethodRanges().stream().map(
                                                r -> Pair.of(r.min(), r.max())
                                        ).collect(Collectors.toList()),
                                        p.getPaymentMethodTypeCode().value()))
                )
                .map(this::docToAggregate);
    }

    public Mono<PaymentMethod> retrievePaymentMethodById(String id){
        log.debug("[Payment Method Aggregate] Retrieve Aggregate");

        return paymentMethodRepository.findByPaymentMethodID(id)
                .map(this::docToAggregate);
    }

    private PaymentMethod docToAggregate(PaymentMethodDocument doc){
        return new PaymentMethod(
                new PaymentMethodID(UUID.fromString(doc.getPaymentMethodID())),
                new PaymentMethodName(doc.getPaymentMethodName()),
                new PaymentMethodDescription(doc.getPaymentMethodDescription()),
                new PaymentMethodStatus(PaymentMethodStatusEnum.valueOf(doc.getPaymentMethodStatus())),
                doc.getPaymentMethodRanges().stream().map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond())).collect(Collectors.toList()),
                new PaymentMethodType(doc.getPaymentMethodTypeCode()));
    }
}
