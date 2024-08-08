package it.pagopa.ecommerce.payment.methods.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.generated.ecommerce.gec.v1.ApiClient;
import it.pagopa.generated.ecommerce.gec.v1.api.CalculatorApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientsConfig implements WebFluxConfigurer {
    private final int maxMemorySize;

    public WebClientsConfig(
            @Value("${afm.client.maxInMemory}") int maxMemorySize
    ) {
        this.maxMemorySize = maxMemorySize;
    }

    @Bean(name = "afmWebClient")
    public CalculatorApi afmWebClient(
                                      @Value("${afm.uri}") String afmWebClientUri,
                                      @Value(
                                          "${afm.readTimeout}"
                                      ) int afmWebClientReadTimeout,
                                      @Value(
                                          "${afm.connectionTimeout}"
                                      ) int afmWebClientConnectionTimeout
    ) {
        final var webClient = createWebClient(
                afmWebClientUri,
                createClientWithTimeouts(afmWebClientReadTimeout, afmWebClientConnectionTimeout)
        );
        return new CalculatorApi(new ApiClient(webClient));
    }

    @Bean(name = "afmWebClientV2")
    public it.pagopa.generated.ecommerce.gec.v2.api.CalculatorApi afmWebClientV2(
                                                                                 @Value(
                                                                                     "${afm.uri.v2}"
                                                                                 ) String afmWebClientUri,
                                                                                 @Value(
                                                                                     "${afm.readTimeout}"
                                                                                 ) int afmWebClientReadTimeout,
                                                                                 @Value(
                                                                                     "${afm.connectionTimeout}"
                                                                                 ) int afmWebClientConnectionTimeout
    ) {
        final var webClient = createWebClient(
                afmWebClientUri,
                createClientWithTimeouts(afmWebClientReadTimeout, afmWebClientConnectionTimeout)
        );
        return new it.pagopa.generated.ecommerce.gec.v2.api.CalculatorApi(
                new it.pagopa.generated.ecommerce.gec.v2.ApiClient(webClient)
        );
    }

    private WebClient createWebClient(
                                      String uri,
                                      HttpClient httpClient
    ) {
        return ApiClient.buildWebClientBuilder().exchangeStrategies(
                ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxMemorySize))
                        .build()
        ).clientConnector(
                new ReactorClientHttpConnector(httpClient)
        ).baseUrl(uri).build();
    }

    private HttpClient createClientWithTimeouts(
                                                int readTimeout,
                                                int connectionTimeout
    ) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .doOnConnected(
                        connection -> connection.addHandlerLast(
                                new ReadTimeoutHandler(
                                        readTimeout,
                                        TimeUnit.MILLISECONDS
                                )
                        )
                ).resolver(nameResolverSpec -> nameResolverSpec.ndots(1));
    }
}
