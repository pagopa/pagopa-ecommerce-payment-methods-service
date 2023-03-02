package it.pagopa.ecommerce.payment.methods.client;

import it.pagopa.generated.ecommerce.gec.v1.api.CalculatorApi;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AfmClient {

    private final CalculatorApi calculatorApi;

    private final String afmKey;

    @Autowired
    public AfmClient(
            @Qualifier("afmWebClient") CalculatorApi afmClient,
            @Value("${afm.client.key}") String afmKey
    ) {
        this.calculatorApi = afmClient;
        this.afmKey = afmKey;
    }

    public Mono<BundleOptionDto> getFees(
                                         PaymentOptionDto paymentOptionDto,
                                         Integer maxOccurrences
    ) {
        return calculatorApi
                .getApiClient()
                .getWebClient()
                .post()
                .uri(
                        uriBuilder -> uriBuilder
                                .queryParam("maxOccurrences", maxOccurrences)
                                .build()
                )
                .header("ocp-apim-subscription-key", afmKey)
                .body(Mono.just(paymentOptionDto), PaymentOptionDto.class)
                .retrieve()
                .bodyToMono(BundleOptionDto.class)
                .doOnError(
                        ResponseStatusException.class,
                        error -> log.error(
                                "ResponseStatus Error : {}",
                                error
                        )
                )
                .doOnError(
                        Exception.class,
                        (Exception error) -> log.error(
                                "Generic Error : {}",
                                error
                        )
                );
    }

}
