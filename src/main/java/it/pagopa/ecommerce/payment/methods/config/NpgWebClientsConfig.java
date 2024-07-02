package it.pagopa.ecommerce.payment.methods.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.api.PaymentServicesApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class NpgWebClientsConfig implements WebFluxConfigurer {

    @Bean(name = "npgWebClient")
    public PaymentServicesApi npgWebClient(
                                           @Value("${npg.uri}") String npgClientUrl,
                                           @Value(
                                               "${npg.readTimeout}"
                                           ) int npgWebClientReadTimeout,
                                           @Value(
                                               "${npg.connectionTimeout}"
                                           ) int npgWebClientConnectionTimeout,
                                           @Value(
                                               "${npg.keepalive.enabled}"
                                           ) boolean keepAliveEnabled,
                                           @Value(
                                               "${npg.keepalive.idle}"
                                           ) int keepAliveIdle,
                                           @Value(
                                               "${npg.keepalive.intvl}"
                                           ) int keepAliveIntvl,
                                           @Value(
                                               "${npg.keepalive.cnt}"
                                           ) int keepAliveCnt

    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, npgWebClientConnectionTimeout)
                .option(ChannelOption.SO_KEEPALIVE, keepAliveEnabled)
                .option(EpollChannelOption.TCP_KEEPIDLE, keepAliveIdle)
                .option(EpollChannelOption.TCP_KEEPINTVL, keepAliveIntvl)
                .option(EpollChannelOption.TCP_KEEPCNT, keepAliveCnt)
                .doOnConnected(
                        connection -> connection.addHandlerLast(
                                new ReadTimeoutHandler(
                                        npgWebClientReadTimeout,
                                        TimeUnit.MILLISECONDS
                                )
                        )
                )
                .resolver(nameResolverSpec -> nameResolverSpec.ndots(1));

        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        WebClient webClient = ApiClient.buildWebClientBuilder().clientConnector(
                new ReactorClientHttpConnector(httpClient)
        ).uriBuilderFactory(defaultUriBuilderFactory).baseUrl(npgClientUrl).build();

        return new PaymentServicesApi(new ApiClient(webClient).setBasePath(npgClientUrl));
    }

    @Bean
    public NpgClient npgClient(
                               PaymentServicesApi paymentServicesApi,

                               Tracer tracer,
                               ObjectMapper objectMapper
    ) {
        return new NpgClient(paymentServicesApi, tracer, objectMapper);
    }
}
