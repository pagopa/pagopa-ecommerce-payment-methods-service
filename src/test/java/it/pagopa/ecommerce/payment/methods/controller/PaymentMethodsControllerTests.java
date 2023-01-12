package it.pagopa.ecommerce.payment.methods.controller;

import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.application.PspService;
import it.pagopa.ecommerce.payment.methods.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.server.model.PSPsResponseDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodResponseDto;
import it.pagopa.ecommerce.payment.methods.server.model.PspDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PaymentMethodsController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
class PaymentMethodsControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private PspService pspService;

    @MockBean
    private ApiConfigClient apiConfigClient;

    @MockBean
    private PaymentMethodService paymentMethodService;

    @Test
    void shouldCreateNewmethod() {
        PaymentMethodRequestDto paymentMethodRequestDto = TestUtil.getPaymentMethodRequest();

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        PaymentMethodResponseDto methodResponse = TestUtil.getPaymentMethodResponse(paymentMethod);

        Mockito.when(paymentMethodService.createPaymentMethod(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(paymentMethod));

        webClient
                .post().uri("/payment-methods")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentMethodRequestDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentMethodResponseDto.class)
                .isEqualTo(methodResponse);
    }

    @Test
    void shouldGetAllMethods() {

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(paymentMethodService.retrievePaymentMethods((int) TestUtil.getTestAmount())).thenReturn(
                Flux.just(paymentMethod)
        );

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        webClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/payment-methods")
                                .queryParam("amount", TestUtil.getTestAmount())
                                .build()
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentMethodResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    void shouldGetPSPs() {

        PspDto pspDto = TestUtil.getTestPspDto();

        Mockito.when(pspService.retrievePsps(null, null, null))
                .thenReturn(Flux.just(pspDto));

        PSPsResponseDto expectedResult = new PSPsResponseDto()
                .psp(List.of(pspDto));

        webClient
                .get()
                .uri("/payment-methods/psps")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PSPsResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldGetAMethod() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(
                paymentMethodService.retrievePaymentMethodById(
                        paymentMethod.getPaymentMethodID().value().toString()
                )
        ).thenReturn(Mono.just(paymentMethod));

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentMethodResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    void shouldGetPaymentMethodPSPs() {
        String TEST_METHOD_ID = UUID.randomUUID().toString();
        PspDto pspDto = TestUtil.getTestPspDto();

        Mockito.when(
                paymentMethodService.retrievePaymentMethodById(
                        any()
                )
        ).thenReturn(Mono.just(TestUtil.getPaymentMethod()));

        Mockito.when(pspService.retrievePsps(any(), any(), any()))
                .thenReturn(Flux.just(pspDto));

        PSPsResponseDto expectedResult = new PSPsResponseDto()
                .psp(List.of(pspDto));

        webClient
                .get()
                .uri("/payment-methods/" + TEST_METHOD_ID + "/psps")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PSPsResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldPatchPaymentMethod() {
        UUID TEST_CAT = UUID.randomUUID();

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(
                paymentMethodService.updatePaymentMethodStatus(
                        paymentMethod.getPaymentMethodID().value().toString(),
                        PaymentMethodStatusEnum.ENABLED
                )
        ).thenReturn(Mono.just(paymentMethod));

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        PaymentMethodRequestDto patchRequest = new PaymentMethodRequestDto()
                .status(PaymentMethodRequestDto.StatusEnum.ENABLED);

        webClient
                .patch().uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patchRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentMethodResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldScheduleUpdate() {

        Mockito.when(apiConfigClient.getPSPs(any(), any())).thenReturn(
                Mono.just(TestUtil.getTestServices())
        );

        webClient
                .put().uri("/payment-methods/psps")
                .exchange()
                .expectStatus()
                .isAccepted();

        Mockito.verify(pspService, Mockito.times(1)).updatePSPs(any());
    }
}
