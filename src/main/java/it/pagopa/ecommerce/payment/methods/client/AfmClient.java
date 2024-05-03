package it.pagopa.ecommerce.payment.methods.client;

import it.pagopa.ecommerce.payment.methods.exception.AfmResponseException;
import it.pagopa.generated.ecommerce.gec.v1.api.CalculatorApi;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.PaymentOptionDto;
import it.pagopa.generated.ecommerce.gec.v2.dto.PaymentOptionMultiDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AfmClient {

    private final CalculatorApi calculatorApi;
    private final it.pagopa.generated.ecommerce.gec.v2.api.CalculatorApi calculatorApiV2;

    private final String afmKey;

    @Autowired
    public AfmClient(
            @Qualifier("afmWebClient") CalculatorApi afmClient,
            @Qualifier("afmWebClientV2") it.pagopa.generated.ecommerce.gec.v2.api.CalculatorApi afmWebClientV2,
            @Value("${afm.client.key}") String afmKey
    ) {
        this.calculatorApi = afmClient;
        this.calculatorApiV2 = afmWebClientV2;
        this.afmKey = afmKey;
    }

    public Mono<BundleOptionDto> getFees(
                                         PaymentOptionDto paymentOptionDto,
                                         Integer maxOccurrences,
                                         boolean allCCP
    ) {
        return calculatorApi
                .getApiClient()
                .getWebClient()
                .post()
                .uri(
                        uriBuilder -> uriBuilder
                                .queryParam("allCcp", allCCP)
                                .queryParam("maxOccurrences", maxOccurrences)
                                .build()
                )
                .header("ocp-apim-subscription-key", afmKey)
                .body(Mono.just(paymentOptionDto), PaymentOptionDto.class)
                .retrieve()
                .onStatus(
                        HttpStatus::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(
                                        errorResponseBody -> Mono.error(
                                                new AfmResponseException(
                                                        clientResponse.statusCode(),
                                                        errorResponseBody
                                                )
                                        )
                                )
                )
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

    public Mono<it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto> getFeesForNotices(
                                                                                            PaymentOptionMultiDto paymentOptionDto,
                                                                                            Integer maxOccurrences,
                                                                                            boolean allCCP
    ) {
        return calculatorApiV2
                .getApiClient()
                .getWebClient()
                .post()
                .uri(
                        uriBuilder -> uriBuilder
                                .queryParam("allCcp", allCCP)
                                .queryParam("maxOccurrences", maxOccurrences)
                                .build()
                )
                .header("ocp-apim-subscription-key", afmKey)
                .body(Mono.just(paymentOptionDto), PaymentOptionMultiDto.class)
                .retrieve()
                .onStatus(
                        HttpStatus::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(
                                        errorResponseBody -> Mono.error(
                                                new AfmResponseException(
                                                        clientResponse.statusCode(),
                                                        errorResponseBody
                                                )
                                        )
                                )
                )
                .bodyToMono(it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto.class)
                .doOnError(
                        ResponseStatusException.class,
                        error -> log.error(
                                String.format("ResponseStatus Error. Status: [%s]", error.getStatus()),
                                error
                        )
                )
                .doOnError(
                        Exception.class,
                        (Exception error) -> log.error(
                                "Generic Error",
                                error
                        )
                );
    }
}
