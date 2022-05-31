package it.pagopa.ecommerce.payment.instruments.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.generated.ecommerce.apiconfig.v1.ApiClient;
import it.pagopa.generated.ecommerce.apiconfig.v1.api.PaymentServiceProvidersApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

public class WebClientsConfig {

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

        WebClient webClient = ApiClient.buildWebClientBuilder().clientConnector(
                new ReactorClientHttpConnector(httpClient)).baseUrl(apiConfigWebClientUri).build();

        return new PaymentServiceProvidersApi(new ApiClient(webClient));
    }
}
