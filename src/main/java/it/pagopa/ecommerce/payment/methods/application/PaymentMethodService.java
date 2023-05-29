package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodAsset;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodID;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodRange;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodStatus;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodType;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.BundleDto;
import it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.CalculateFeeResponseDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodStatusDto;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.generated.ecommerce.gec.v1.dto.PspSearchCriteriaDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferListItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ApplicationService
@Slf4j
public class PaymentMethodService {

    private final AfmClient afmClient;

    private PaymentMethodRepository paymentMethodRepository;

    private PaymentMethodFactory paymentMethodFactory;

    @Autowired
    public PaymentMethodService(
            AfmClient afmClient,
            PaymentMethodRepository paymentMethodRepository,
            PaymentMethodFactory paymentMethodFactory
    ) {
        this.afmClient = afmClient;
        this.paymentMethodFactory = paymentMethodFactory;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public Mono<PaymentMethod> createPaymentMethod(
                                                   String paymentMethodName,
                                                   String paymentMethodDescription,
                                                   List<Pair<Long, Long>> ranges,
                                                   String paymentMethodTypeCode,
                                                   String paymentMethodAsset
    ) {
        log.info("[Payment Method Aggregate] Create new aggregate");
        Mono<PaymentMethod> paymentMethod = paymentMethodFactory.newPaymentMethod(
                new PaymentMethodID(UUID.randomUUID()),
                new PaymentMethodName(paymentMethodName),
                new PaymentMethodDescription(paymentMethodDescription),
                new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                ranges.stream().map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond())).toList(),
                new PaymentMethodType(paymentMethodTypeCode),
                new PaymentMethodAsset(paymentMethodAsset)
        );

        log.info("[Payment Method Aggregate] Store new aggregate");

        return paymentMethod.flatMap(
                p -> paymentMethodRepository.save(
                        new PaymentMethodDocument(
                                p.getPaymentMethodID().value().toString(),
                                p.getPaymentMethodName().value(),
                                p.getPaymentMethodDescription().value(),
                                p.getPaymentMethodStatus().value().toString(),
                                p.getPaymentMethodAsset().value(),
                                p.getPaymentMethodRanges().stream().map(r -> Pair.of(r.min(), r.max()))
                                        .toList(),
                                p.getPaymentMethodTypeCode().value()
                        )
                ).map(
                        doc -> new PaymentMethod(
                                new PaymentMethodID(UUID.fromString(doc.getPaymentMethodID())),
                                new PaymentMethodName(doc.getPaymentMethodName()),
                                new PaymentMethodDescription(doc.getPaymentMethodDescription()),
                                new PaymentMethodStatus(PaymentMethodStatusEnum.valueOf(doc.getPaymentMethodStatus())),
                                new PaymentMethodType(doc.getPaymentMethodTypeCode()),
                                doc.getPaymentMethodRanges().stream()
                                        .map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond()))
                                        .toList(),
                                new PaymentMethodAsset(doc.getPaymentMethodAsset())
                        )
                )
        );
    }

    public Flux<PaymentMethod> retrievePaymentMethods(Integer amount) {
        log.info("[Payment Method Aggregate] Retrieve Aggregate");

        if (amount == null) {
            return paymentMethodRepository.findAll().map(this::docToAggregate);
        } else {
            return paymentMethodRepository
                    .findAll()
                    .filter(
                            doc -> doc.getPaymentMethodRanges().stream()
                                    .anyMatch(
                                            range -> range.getFirst().longValue() <= amount
                                                    && range.getSecond().longValue() >= amount
                                    )
                    ).map(this::docToAggregate);
        }
    }

    public Mono<PaymentMethod> updatePaymentMethodStatus(
                                                         String id,
                                                         PaymentMethodStatusEnum status
    ) {
        log.info("[Payment method Aggregate] Patch Aggregate");

        return paymentMethodRepository
                .findById(id)
                .map(this::docToAggregate)
                .switchIfEmpty(
                        Mono.error(new PaymentMethodNotFoundException(id))
                )
                .map(p -> {
                    p.setPaymentMethodStatus(status);
                    return p;
                })
                .flatMap(
                        p -> paymentMethodRepository
                                .save(
                                        new PaymentMethodDocument(
                                                p.getPaymentMethodID().value().toString(),
                                                p.getPaymentMethodName().value(),
                                                p.getPaymentMethodDescription().value(),
                                                p.getPaymentMethodStatus().value().toString(),
                                                p.getPaymentMethodAsset().value(),
                                                p.getPaymentMethodRanges().stream().map(
                                                        r -> Pair.of(r.min(), r.max())
                                                ).toList(),
                                                p.getPaymentMethodTypeCode().value()
                                        )
                                )
                )
                .map(this::docToAggregate);
    }

    public Mono<PaymentMethod> retrievePaymentMethodById(String id) {
        log.info("[Payment Method Aggregate] Retrieve Aggregate");

        return paymentMethodRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(id)))
                .map(this::docToAggregate);
    }

    public Mono<CalculateFeeResponseDto> computeFee(
                                                    Mono<CalculateFeeRequestDto> paymentOptionDto,
                                                    String paymentMethodId,
                                                    Integer maxOccurrences
    ) {
        log.info("[Payment Method] Retrieve bundles list");
        return paymentMethodRepository.findById(paymentMethodId)
                .switchIfEmpty(Mono.error(new PaymentMethodNotFoundException(paymentMethodId)))
                .flatMap(
                        pm -> paymentOptionDto.map(
                                po -> new it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto()
                                        .bin(po.getBin())
                                        .paymentAmount(po.getPaymentAmount())
                                        .idPspList(
                                                Optional.ofNullable(po.getIdPspList()).orElseGet(ArrayList::new)
                                                        .stream()
                                                        .map(idPsp -> new PspSearchCriteriaDto().idPsp(idPsp))
                                                        .toList()
                                        )
                                        .paymentMethod(pm.getPaymentMethodTypeCode())
                                        .primaryCreditorInstitution(po.getPrimaryCreditorInstitution())
                                        .touchpoint(po.getTouchpoint())
                                        .transferList(
                                                po.getTransferList()
                                                        .stream()
                                                        .map(
                                                                t -> new TransferListItemDto()
                                                                        .creditorInstitution(t.getCreditorInstitution())
                                                                        .digitalStamp(t.getDigitalStamp())
                                                                        .transferCategory(t.getTransferCategory())
                                                        )
                                                        .toList()
                                        )

                        ).flatMap(reqBody -> afmClient.getFees(reqBody, maxOccurrences))
                                .map(bo -> {
                                    bo.setBundleOptions(
                                            removeDuplicatePsp(bo.getBundleOptions())
                                    );
                                    return bo;
                                })
                                .map(bo -> bundleOptionToResponse(bo, pm))

                );

    }

    private List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> removeDuplicatePsp(
                                                                                          List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transfers
    ) {
        List<String> idPsps = new ArrayList<>();

        return transfers
                .stream()
                .filter(t -> {
                    if (idPsps.contains(t.getIdPsp())) {
                        return false;
                    } else {
                        idPsps.add(t.getIdPsp());
                        return true;
                    }
                })
                .toList();

    }

    private CalculateFeeResponseDto bundleOptionToResponse(
                                                           it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto bundle,
                                                           PaymentMethodDocument paymentMethodDocument
    ) {
        return new CalculateFeeResponseDto()
                .belowThreshold(bundle.getBelowThreshold())
                .paymentMethodName(paymentMethodDocument.getPaymentMethodName())
                .paymentMethodStatus(PaymentMethodStatusDto.valueOf(paymentMethodDocument.getPaymentMethodStatus()))
                .bundles(
                        bundle.getBundleOptions() != null ? bundle.getBundleOptions()
                                .stream()
                                .map(
                                        t -> new BundleDto()
                                                .abi(t.getAbi())
                                                .bundleDescription(t.getBundleDescription())
                                                .bundleName(t.getBundleName())
                                                .idBrokerPsp(t.getIdBrokerPsp())
                                                .idBundle(t.getIdBundle())
                                                .idChannel(t.getIdChannel())
                                                .idCiBundle(t.getIdCiBundle())
                                                .idPsp(t.getIdPsp())
                                                .onUs(t.getOnUs())
                                                .paymentMethod(
                                                        // A null value is considered as "any" in the AFM domain
                                                        t.getPaymentMethod() == null
                                                                ? paymentMethodDocument.getPaymentMethodTypeCode()
                                                                : t.getPaymentMethod()
                                                )
                                                .primaryCiIncurredFee(t.getPrimaryCiIncurredFee())
                                                .taxPayerFee(t.getTaxPayerFee())
                                                .touchpoint(t.getTouchpoint())
                                ).toList() : new ArrayList<>()
                );
    }

    private PaymentMethod docToAggregate(PaymentMethodDocument doc) {
        if (doc == null) {
            return null;
        }

        return new PaymentMethod(
                new PaymentMethodID(UUID.fromString(doc.getPaymentMethodID())),
                new PaymentMethodName(doc.getPaymentMethodName()),
                new PaymentMethodDescription(doc.getPaymentMethodDescription()),
                new PaymentMethodStatus(PaymentMethodStatusEnum.valueOf(doc.getPaymentMethodStatus())),
                new PaymentMethodType(doc.getPaymentMethodTypeCode()),
                doc.getPaymentMethodRanges().stream()
                        .map(pair -> new PaymentMethodRange(pair.getFirst(), pair.getSecond()))
                        .collect(Collectors.toList()),
                new PaymentMethodAsset(doc.getPaymentMethodAsset())
        );
    }

}
