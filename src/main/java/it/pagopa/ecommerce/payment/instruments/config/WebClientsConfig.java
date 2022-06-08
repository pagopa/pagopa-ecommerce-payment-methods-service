package it.pagopa.ecommerce.payment.instruments.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.generated.ecommerce.apiconfig.v1.ApiClient;
import it.pagopa.generated.ecommerce.apiconfig.v1.api.PaymentServiceProvidersApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;
@Configuration
public class WebClientsConfig implements WebFluxConfigurer {
    @Value("${apiConfig.client.maxInMemory}")
    private int maxMemorySize;
    @Bean(name = "apiConfigWebClient")
    public PaymentServiceProvidersApi apiConfigWebClient(@Value("${apiConfig.uri}") String apiConfigWebClientUri,
                               @Value("${apiConfig.readTimeout}") int apiConfigWebClientReadTimeout,
                               @Value("${apiConfig.connectionTimeout}") int apiConfigWebClientConnectionTimeout) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, apiConfigWebClientConnectionTimeout)
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(
                                apiConfigWebClientReadTimeout,
                                TimeUnit.MILLISECONDS)));

        WebClient webClient = ApiClient.buildWebClientBuilder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxMemorySize))
                .build()).clientConnector(
                new ReactorClientHttpConnector(httpClient)).baseUrl(apiConfigWebClientUri).build();

        return new PaymentServiceProvidersApi(new ApiClient(webClient));
    }
}
