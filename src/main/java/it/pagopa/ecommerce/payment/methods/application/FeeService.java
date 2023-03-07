package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentOptionDto;
import it.pagopa.ecommerce.payment.methods.server.model.TransferDto;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferListItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@ApplicationService
@Slf4j
public class FeeService {

    private final AfmClient afmClient;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    public FeeService(
            AfmClient afmClient,
            PaymentMethodService paymentMethodService
    ) {
        this.afmClient = afmClient;
        this.paymentMethodService = paymentMethodService;
    }

    public Mono<it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto> computeFee(
                                                                                             Mono<PaymentOptionDto> paymentOptionDto,
                                                                                             Integer maxOccurrences
    ) {
        return paymentOptionDto.map(
                po -> new it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto()
                        .bin(po.getBin())
                        .paymentAmount(po.getPaymentAmount())
                        .idPspList(po.getIdPspList())
                        .paymentMethod(po.getPaymentMethod())
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
                                        .collect(toList())
                        )

        ).flatMap(reqBody -> afmClient.getFees(reqBody, maxOccurrences))
                .map(bo -> {
                    bo.setBundleOptions(
                            removeDuplicatePsp(bo.getBundleOptions())
                    );
                    return bo;
                })
                .flatMap(
                        bo -> paymentMethodService.removeDisabledPsp(bo.getBundleOptions())
                                .map(filtered -> {
                                    bo.setBundleOptions(filtered);
                                    return bo;
                                })
                )
                .map(this::bundleOptionToResponse);
    }

    public List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> removeDuplicatePsp(
                                                                                         List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transfers
    ) {
        return transfers
                .stream()
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toCollection(
                                        () -> new TreeSet<>(
                                                Comparator.comparing(
                                                        it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto::getIdPsp
                                                )
                                        )
                                ),
                                ArrayList::new
                        )
                );
    }

    public Map<String, List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto>> groupBundleByPsp(
                                                                                                    it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto bundleOptionDto
    ) {
        return bundleOptionDto.getBundleOptions()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto::getIdPsp,
                                Collectors.mapping(
                                        Function.identity(),
                                        Collectors.collectingAndThen(
                                                toList(),
                                                e -> e.stream().sorted(
                                                        Comparator.comparingLong(
                                                                it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto::getTaxPayerFee
                                                        )
                                                )
                                                        .collect(toList())
                                        )
                                )
                        )
                );
    }

    private BundleOptionDto bundleOptionToResponse(
                                                   it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto bundle
    ) {
        return new it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto()
                .belowThreshold(bundle.getBelowThreshold())
                .bundleOptions(
                        bundle.getBundleOptions() != null ? bundle.getBundleOptions()
                                .stream()
                                .map(
                                        t -> new TransferDto()
                                                .abi(t.getAbi())
                                                .bundleDescription(t.getBundleDescription())
                                                .bundleName(t.getBundleName())
                                                .idBrokerPsp(t.getIdBrokerPsp())
                                                .idBundle(t.getIdBundle())
                                                .idChannel(t.getIdChannel())
                                                .idCiBundle(t.getIdCiBundle())
                                                .idPsp(t.getIdPsp())
                                                .onUs(t.getOnUs())
                                                .paymentMethod(t.getPaymentMethod())
                                                .primaryCiIncurredFee(t.getPrimaryCiIncurredFee())
                                                .taxPayerFee(t.getTaxPayerFee())
                                                .touchpoint(t.getTouchpoint())
                                ).toList() : new ArrayList<>()
                );
    }

}
