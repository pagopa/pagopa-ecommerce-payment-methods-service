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
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AfmClient {

    public static final String HEADER_APIM_KEY = "ocp-apim-subscription-key";

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
                .header(HEADER_APIM_KEY, afmKey)
                .body(Mono.just(paymentOptionDto), PaymentOptionDto.class)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Failure response without body")
                                .flatMap(
                                        errorResponseBody -> Mono.error(
                                                new AfmResponseException(
                                                        HttpStatus.resolve(clientResponse.statusCode().value()),
                                                        errorResponseBody
                                                )
                                        )
                                )
                )
                .bodyToMono(BundleOptionDto.class)
                .doOnError(
                        AfmResponseException.class,
                        error -> log.error(
                                String.format(
                                        "Response error with status: [%s], message: [%s]",
                                        error.status,
                                        error.reason
                                ),
                                error
                        )
                )
                .doOnError(
                        WebClientException.class,
                        error -> log.error(
                                String.format("Client error: [%s]", error.getMessage()),
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
                .header(HEADER_APIM_KEY, afmKey)
                .body(Mono.just(paymentOptionDto), PaymentOptionMultiDto.class)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Failure response without body")
                                .flatMap(
                                        errorResponseBody -> Mono.error(
                                                new AfmResponseException(
                                                        HttpStatus.resolve(clientResponse.statusCode().value()),
                                                        errorResponseBody
                                                )
                                        )
                                )
                )
                .bodyToMono(it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto.class)
                .doOnError(
                        AfmResponseException.class,
                        error -> log.error(
                                String.format(
                                        "Response error with status: [%s], message: [%s]",
                                        error.status,
                                        error.reason
                                ),
                                error
                        )
                )
                .doOnError(
                        WebClientException.class,
                        error -> log.error(
                                String.format("Client error: [%s]", error.getMessage()),
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
