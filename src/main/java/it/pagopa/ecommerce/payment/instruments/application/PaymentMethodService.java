package it.pagopa.ecommerce.payment.instruments.application;

import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.instruments.utils.ApplicationService;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentMethodStatusEnum;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServiceDto;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
                                                   List<Pair<Long, Long>> ranges,
                                                   String paymentMethodTypeCode){
        log.debug("[Payment Method Aggregate] Create new aggregate");
        Mono<PaymentMethod> paymentMethod = paymentInstrumentFactory.newPaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName(paymentMethodName),
                new PaymentMethodDescription(paymentMethodDescription),
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                ranges.stream().map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond())).toList(),
                new PaymentMethodType(paymentMethodTypeCode)
        );

        log.debug("[Payment Method Aggregate] Store new aggregate");

        return paymentMethod.flatMap(p -> paymentMethodRepository.save(
                new PaymentMethodDocument(p.getPaymentMethodID().value().toString(),
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

        if (amount == null){
            return paymentMethodRepository.findAll().map(this::docToAggregate);
        } else {
            return paymentMethodRepository
                    .findAll()
                    .filter(doc -> doc.getPaymentMethodRanges().stream()
                            .anyMatch(range -> range.getFirst() <= amount && range.getSecond() >= amount)
                    ).map(this::docToAggregate);
        }
    }

    public Mono<PaymentMethod> updatePaymentMethodStatus(String id,
                                                         PaymentMethodStatusEnum status){
        log.debug("[Payment instrument Aggregate] Patch Aggregate");

        return paymentMethodRepository
                .findById(id)
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

        return paymentMethodRepository
                .findById(id)
                .map(this::docToAggregate);
    }

    public void updatePaymentMethodRanges(ServicesDto servicesDto){
        Map<String, Set<Pair<Long, Long>>> rangeMap = servicesDto.getServices().stream()
                .collect(Collectors.groupingBy(
                                ServiceDto::getPaymentTypeCode,
                                Collectors.mapping(it ->
                                                Pair.of(
                                                        Double.valueOf(it.getMinimumAmount() * Double.valueOf(100.0)).longValue(), // Convert euros to cents
                                                        Double.valueOf(it.getMaximumAmount() * Double.valueOf(100.0)).longValue()),
                                        Collectors.toSet())
                        )
                );

        rangeMap.keySet().forEach(paymentTypeCode -> {
                    log.info("PaymentTypeCode: {}", paymentTypeCode);

                    paymentMethodRepository.findByPaymentMethodTypeCode(paymentTypeCode)
                            .flatMap(p -> {
                                log.info("Updating paymentMethod: {}", p.getPaymentMethodID());
                                p.setPaymentMethodRanges(rangeMap.get(paymentTypeCode).stream().toList());
                                return Mono.just(p);
                            })
                            .flatMap(updatedDoc -> paymentMethodRepository.save(updatedDoc))
                            .subscribe();
                }
        );
    }

    private PaymentMethod docToAggregate(PaymentMethodDocument doc){
        if(doc == null){
            return null;
        }

        return new PaymentMethod(
                new PaymentMethodID(UUID.fromString(doc.getPaymentMethodID())),
                new PaymentMethodName(doc.getPaymentMethodName()),
                new PaymentMethodDescription(doc.getPaymentMethodDescription()),
                new PaymentMethodStatus(PaymentMethodStatusEnum.valueOf(doc.getPaymentMethodStatus())),
                doc.getPaymentMethodRanges().stream().map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond())).collect(Collectors.toList()),
                new PaymentMethodType(doc.getPaymentMethodTypeCode()));
    }
}
