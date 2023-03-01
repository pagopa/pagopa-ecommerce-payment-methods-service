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

    @Autowired
    @Qualifier("afmWebClient")
    private CalculatorApi afmClient;

    @Value("${afm.client.key}")
    private String afmKey;

    public Mono<BundleOptionDto> getFees(
                                         PaymentOptionDto paymentOptionDto,
                                         Integer maxOccurrences
    ) {
        return afmClient
                .getApiClient()
                .getWebClient()
                .post()
                .uri(
                        uriBuilder -> uriBuilder
                                .queryParam("maxOccurrences", maxOccurrences)
                                .build()
                )
                .body(paymentOptionDto, PaymentOptionDto.class)
                .header("ocp-apim-subscription-key", afmKey)
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
