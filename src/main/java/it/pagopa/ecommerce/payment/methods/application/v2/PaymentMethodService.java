package it.pagopa.ecommerce.payment.methods.application.v2;

import io.vavr.Tuple;
import it.pagopa.ecommerce.payment.methods.application.BundleOptions;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodServiceCommon;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.client.PaymentMethodsHandlerClient;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.utils.ApplicationService;
import it.pagopa.ecommerce.payment.methods.v2.server.model.*;
import it.pagopa.generated.ecommerce.gec.v2.dto.PaymentNoticeItemDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.PaymentOptionMultiDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.PspSearchCriteriaDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.TransferListItemDto;
import it.pagopa.generated.ecommerce.handler.v1.dto.PaymentMethodResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service(PaymentMethodService.QUALIFIER_NAME)
@ApplicationService
@Slf4j
public class PaymentMethodService extends PaymentMethodServiceCommon {

    protected static final String QUALIFIER_NAME = "paymentMethodServiceV2";

    private final AfmClient afmClient;
    private final PaymentMethodsHandlerClient paymentMethodsHandlerClient;

    public PaymentMethodService(
            AfmClient afmClient,
            NpgSessionsTemplateWrapper npgSessionsTemplateWrapper,
            PaymentMethodsHandlerClient paymentMethodsHandlerClient
    ) {
        super(npgSessionsTemplateWrapper, paymentMethodsHandlerClient);
        this.afmClient = afmClient;
        this.paymentMethodsHandlerClient = paymentMethodsHandlerClient;
    }

    public Mono<CalculateFeeResponseDto> computeFee(
                                                    CalculateFeeRequestDto feeRequestDto,
                                                    String paymentMethodId,
                                                    Integer maxOccurrences
    ) {
        log.info(
                "[Payment Method] Retrieve bundles list for payment method: [{}], allCcp: [{}], isMulti: [{}] and payment notice amounts: {}",
                paymentMethodId,
                feeRequestDto.getIsAllCCP(),
                feeRequestDto.getPaymentNotices().size() > 1,
                feeRequestDto.getPaymentNotices().stream().map(PaymentNoticeDto::getPaymentAmount).toList()
        );
        return paymentMethodsHandlerClient.validatePaymentMethodExists(paymentMethodId, null)
                .flatMap(
                        paymentMethod -> afmClient.getFeesForNotices(
                                createGecFeeRequest(paymentMethod, feeRequestDto),
                                maxOccurrences,
                                feeRequestDto.getIsAllCCP()
                        ).map(bundle -> Tuple.of(paymentMethod, bundle))
                )
                .map(bundleAndPaymentMethod -> bundleAndPaymentMethod.map2(BundleOptions::removeDuplicatePspV2))
                .map(
                        bundleAndPaymentMethod -> bundleOptionToResponse(
                                bundleAndPaymentMethod._2(),
                                bundleAndPaymentMethod._1()
                        )
                )
                .filter(response -> !response.getBundles().isEmpty())
                .switchIfEmpty(
                        Mono.error(
                                new NoBundleFoundException(
                                        paymentMethodId,
                                        feeRequestDto.getPaymentNotices().stream()
                                                .map(PaymentNoticeDto::getPaymentAmount).reduce(Long::sum).orElse(0L),
                                        feeRequestDto.getTouchpoint()
                                )
                        )
                )
                .doOnError(
                        NoBundleFoundException.class,
                        error -> log
                                .error(String.format("No bundle found for payment method [%s]", paymentMethodId), error)
                );
    }

    private PaymentOptionMultiDto createGecFeeRequest(
                                                      PaymentMethodResponseDto paymentMethod,
                                                      CalculateFeeRequestDto feeRequestDto
    ) {
        final var paymentNotices = feeRequestDto.getPaymentNotices().stream()
                .map(
                        it -> new PaymentNoticeItemDto()
                                .paymentAmount(it.getPaymentAmount())
                                .primaryCreditorInstitution(it.getPrimaryCreditorInstitution())
                                .transferList(
                                        it.getTransferList()
                                                .stream()
                                                .map(
                                                        t -> new TransferListItemDto()
                                                                .creditorInstitution(
                                                                        t.getCreditorInstitution()
                                                                )
                                                                .digitalStamp(t.getDigitalStamp())
                                                                .transferCategory(
                                                                        t.getTransferCategory()
                                                                )
                                                ).toList()
                                )
                )
                .toList();

        return new PaymentOptionMultiDto()
                .bin(feeRequestDto.getBin())
                .idPspList(
                        Optional.ofNullable(feeRequestDto.getIdPspList()).orElseGet(
                                ArrayList::new
                        )
                                .stream()
                                .map(idPsp -> new PspSearchCriteriaDto().idPsp(idPsp))
                                .toList()
                )
                .paymentMethod(paymentMethod.getPaymentTypeCode())
                .touchpoint(feeRequestDto.getTouchpoint())
                .paymentNotice(paymentNotices);
    }

    private CalculateFeeResponseDto bundleOptionToResponse(
                                                           it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto bundle,
                                                           PaymentMethodResponseDto paymentMethod
    ) {
        final var bundles = Optional.ofNullable(bundle.getBundleOptions())
                .orElse(List.of())
                .stream()
                .map(
                        t -> new BundleDto()
                                .abi(t.getAbi())
                                .bundleDescription(t.getBundleDescription())
                                .bundleName(t.getBundleName())
                                .idBrokerPsp(t.getIdBrokerPsp())
                                .idBundle(t.getIdBundle())
                                .idChannel(t.getIdChannel())
                                .idPsp(t.getIdPsp())
                                .onUs(t.getOnUs())
                                .paymentMethod(
                                        // A null value is considered as "any" in the AFM domain
                                        Optional.ofNullable(t.getPaymentMethod())
                                                .orElse(paymentMethod.getPaymentTypeCode())
                                )
                                .taxPayerFee(t.getTaxPayerFee())
                                .touchpoint(t.getTouchpoint())
                                .pspBusinessName(t.getPspBusinessName())
                ).toList();

        return new CalculateFeeResponseDto()
                .belowThreshold(bundle.getBelowThreshold())
                .paymentMethodName(
                        paymentMethod.getName()
                                .getOrDefault("it", paymentMethod.getName().values().stream().findFirst().orElse(""))
                )
                .paymentMethodDescription(
                        paymentMethod.getDescription().getOrDefault(
                                "it",
                                paymentMethod.getDescription().values().stream().findFirst().orElse("")
                        )
                )
                .paymentMethodStatus(PaymentMethodStatusDto.valueOf(paymentMethod.getStatus().getValue()))
                .bundles(bundles)
                .asset(paymentMethod.getPaymentMethodAsset())
                .brandAssets(paymentMethod.getPaymentMethodsBrandAssets());
    }

}
