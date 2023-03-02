package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PspFactory;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentOptionDto;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferListItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@ApplicationService
@Slf4j
public class FeeService {
    @Autowired
    private PspFactory pspFactory;

    @Autowired
    private AfmClient afmClient;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public Mono<BundleOptionDto> computeFee(
                                            Mono<PaymentOptionDto> paymentOptionDto,
                                            Integer maxOccurrences
    ) {
        return paymentOptionDto.map(
                po -> new it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto()
                        .bin(po.getBin())
                        .paymentAmount(po.getPaymentAmount())
                        .idPspList(po.getIdPspList())
                        .paymentMethod(po.getPaymentMethodId())
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
                                        .collect(Collectors.toList())
                        )

        ).flatMap(reqBody -> afmClient.getFees(reqBody, maxOccurrences));

    }

}
