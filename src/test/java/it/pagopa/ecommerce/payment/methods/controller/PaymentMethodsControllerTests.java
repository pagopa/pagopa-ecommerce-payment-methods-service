package it.pagopa.ecommerce.payment.methods.controller;

import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.application.PspService;
import it.pagopa.ecommerce.payment.methods.client.ApiConfigClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;


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
    void shouldCreateNewmethod(){
        String TEST_NAME = "Test";
        String TEST_DESC = "test";
        PaymentMethodRequestDto.StatusEnum TEST_STATUS = PaymentMethodRequestDto.StatusEnum.ENABLED;
        UUID TEST_CAT = UUID.randomUUID();
        String TEST_TYPE_CODE = "test";

        PaymentMethodRequestDto paymentmethodRequestDto = TestUtil.getPaymentMethodRequest();

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(paymentMethodService.createPaymentMethod(TEST_NAME, TEST_DESC, List.of(Pair.of(0L, 100L)), TEST_TYPE_CODE))
                .thenReturn(Mono.just(paymentMethod));

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        webClient
                .post().uri("/payment-methods")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentmethodRequestDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentMethodResponseDto.class)
                .isEqualTo(expectedResult);
    }


    @Test
    void shouldGetAllmethods(){

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(paymentMethodService.retrievePaymentMethods((int) TestUtil.getTestAmount())).thenReturn(
                Flux.just(paymentMethod)
        );

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/payment-methods")
                    .queryParam("amount", TestUtil.getTestAmount())
                    .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentMethodResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    void shouldGetPSPs(){

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
    void shouldGetAnmethod(){
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(paymentMethodService.retrievePaymentMethodById(
                paymentMethod.getPaymentMethodID().value().toString())
        ).thenReturn(Mono.just(paymentMethod));

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        webClient
                .get()
                .uri("/payment-methods/"+paymentMethod.getPaymentMethodID().value())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentMethodResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    void shouldGetPaymentmethodPSPs(){
        String TEST_method_ID = UUID.randomUUID().toString();
        PspDto pspDto = TestUtil.getTestPspDto();

        Mockito.when(paymentMethodService.retrievePaymentMethodById(
                        Mockito.any()
                )
        ).thenReturn(Mono.just(TestUtil.getPaymentMethod()));

        Mockito.when(pspService.retrievePsps(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Flux.just(pspDto));

        PSPsResponseDto expectedResult = new PSPsResponseDto()
                .psp(List.of(pspDto));

        webClient
                .get()
                .uri("/payment-methods/"+TEST_method_ID+"/psps")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PSPsResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldPatchPaymentmethod(){
        UUID TEST_CAT = UUID.randomUUID();

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(paymentMethodService.updatePaymentMethodStatus(
                paymentMethod.getPaymentMethodID().value().toString(), PaymentMethodStatusEnum.ENABLED)
        ).thenReturn(Mono.just(paymentMethod));

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        PaymentMethodRequestDto patchRequest = new PaymentMethodRequestDto()
                .status(PaymentMethodRequestDto.StatusEnum.ENABLED);

        webClient
                .patch().uri("/payment-methods/"+paymentMethod.getPaymentMethodID().value())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patchRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentMethodResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldScheduleUpdate(){

        Mockito.when(apiConfigClient.getPSPs(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(
                Mono.just(TestUtil.getTestServices())
        );

        webClient
                .put().uri("/payment-methods/psps")
                .exchange()
                .expectStatus()
                .isAccepted();

        Mockito.verify(pspService, Mockito.times(1)).updatePSPs(Mockito.any());
    }
}
