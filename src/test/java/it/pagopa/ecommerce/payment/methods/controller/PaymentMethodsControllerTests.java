package it.pagopa.ecommerce.payment.methods.controller;

import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;
import it.pagopa.ecommerce.payment.methods.exception.AfmResponseException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodAlreadyInUseException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.exception.SessionIdNotFoundException;
import it.pagopa.ecommerce.payment.methods.server.model.*;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PaymentMethodsController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
class PaymentMethodsControllerTests {

    @InjectMocks
    private PaymentMethodsController paymentMethodsController = new PaymentMethodsController();
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private PaymentMethodService paymentMethodService;

    @MockBean
    private Tracer tracer;

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

        PaymentMethodsResponseDto expectedResult = TestUtil.getPaymentMethodsResponse(paymentMethod);

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
                .expectBodyList(PaymentMethodsResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
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
                .status(PaymentMethodStatusDto.ENABLED);

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
    void shouldGetFees() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto requestBody = TestUtil.getCalculateFeeRequest();
        CalculateFeeResponseDto serviceResponse = TestUtil
                .getCalculateFeeResponseFromClientResponse(TestUtil.getBundleOptionDtoClientResponse());
        Mockito.when(paymentMethodService.computeFee(any(), any(), any()))
                .thenReturn(Mono.just(serviceResponse));

        webClient
                .post()
                .uri("/payment-methods/" + paymentMethodId + "/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CalculateFeeResponseDto.class)
                .isEqualTo(serviceResponse);
    }

    @Test
    void shouldRetrieveCardDataFromWithSessionId() {
        String paymentMethodId = "paymentMethodId";
        String sessionId = "sessionId";
        SessionPaymentMethodResponseDto response = new SessionPaymentMethodResponseDto().sessionId(sessionId)
                .bin("123456").brand("VISA").expiringDate("0424")
                .lastFourDigits("1234");
        Mockito.when(paymentMethodService.getCardDataInformation(paymentMethodId, sessionId))
                .thenReturn(Mono.just(response));

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethodId + "/sessions/" + sessionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SessionPaymentMethodResponseDto.class)
                .isEqualTo(response);
    }

    @Test
    void shouldReturnResponseSessionIdNotFound() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new SessionIdNotFoundException("sessionId"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Session id not found", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnResponseEntityWithNotFound() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new PaymentMethodNotFoundException("paymentMethodId"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Payment method not found", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnResponseEntityWithAlreadyInUse() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new PaymentMethodAlreadyInUseException(new PaymentMethodName("PaymentMethodName")));
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Payment method already in use", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnResponseEntityWithGenericError() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new RuntimeException());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Internal server error", responseEntity.getBody().getTitle());
    }

    @Test
    void shouldReturnResponseEntityWithAfmError() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new AfmResponseException(HttpStatus.NOT_FOUND, "reason test"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("reason test", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnNoContentForValidSession() {
        String paymentMethodId = UUID.randomUUID().toString();
        String sessionId = "sessionId";
        String securityToken = "securityToken";
        SessionValidateRequestDto requestBody = new SessionValidateRequestDto().securityToken(securityToken);
        Mockito.when(paymentMethodService.isSessionValid(sessionId, securityToken, paymentMethodId))
                .thenReturn(Mono.just(Optional.of(true)));

        webClient
                .post()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}/validate")
                                .build(paymentMethodId, sessionId)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    void shouldReturn404ForInvalidSession() {
        String paymentMethodId = UUID.randomUUID().toString();
        String sessionId = "sessionId";
        String securityToken = "securityToken";
        SessionValidateRequestDto requestBody = new SessionValidateRequestDto().securityToken(securityToken);
        Mockito.when(paymentMethodService.isSessionValid(sessionId, securityToken, paymentMethodId))
                .thenReturn(Mono.just(Optional.of(false)));

        ProblemJsonDto expected = new ProblemJsonDto().status(404).title("Not found").detail("Session id not found");

        webClient
                .post()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}/validate")
                                .build(paymentMethodId, sessionId)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturn404ForSessionNotFound() {
        String paymentMethodId = UUID.randomUUID().toString();
        String sessionId = "sessionId";
        String securityToken = "securityToken";
        SessionValidateRequestDto requestBody = new SessionValidateRequestDto().securityToken(securityToken);
        Mockito.when(paymentMethodService.isSessionValid(sessionId, securityToken, paymentMethodId))
                .thenReturn(Mono.just(Optional.empty()));

        ProblemJsonDto expected = new ProblemJsonDto().status(404).title("Not found").detail("Session id not found");

        webClient
                .post()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}/validate")
                                .build(paymentMethodId, sessionId)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }
}
