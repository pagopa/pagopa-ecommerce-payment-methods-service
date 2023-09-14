package it.pagopa.ecommerce.payment.methods.controller;

import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PaymentMethodName;
import it.pagopa.ecommerce.payment.methods.exception.*;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionDocument;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

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
    void shouldReturnResponseWithSuccessfulUpdate() {
        String paymentMethodId = UUID.randomUUID().toString();
        PatchSessionRequestDto requestBody = TestUtil.patchSessionRequest();
        String newTransactionId = requestBody.getTransactionId();

        NpgSessionDocument originalSession = TestUtil.npgSessionDocument("orderId", "sessionId", false, null);
        NpgSessionDocument updatedDocument = TestUtil.patchSessionResponse(originalSession, newTransactionId);

        Mockito.when(paymentMethodService.updateSession(paymentMethodId, originalSession.orderId(), requestBody))
                .thenReturn(Mono.just(updatedDocument));

        webClient
                .patch()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}")
                                .build(paymentMethodId, originalSession.orderId())
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
    void shouldRetrieveCardDataFromWithSessionId() {
        String paymentMethodId = "paymentMethodId";
        String orderId = "orderId";
        SessionPaymentMethodResponseDto response = new SessionPaymentMethodResponseDto()
                .sessionId("sessionId")
                .bin("123456").brand("VISA").expiringDate("0424")
                .lastFourDigits("1234");
        Mockito.when(paymentMethodService.getCardDataInformation(paymentMethodId, orderId))
                .thenReturn(Mono.just(response));

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethodId + "/sessions/" + orderId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SessionPaymentMethodResponseDto.class)
                .isEqualTo(response);
    }

    @Test
    void shouldReturnResponseSessionIdNotFound() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new OrderIdNotFoundException("orderId"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Order id not found", responseEntity.getBody().getDetail());
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
    void shouldReturnTransactionIdForValidSession() {
        String paymentMethodId = UUID.randomUUID().toString();
        String orderId = "orderId";
        String securityToken = "securityToken";
        String transactionId = "transactionId";

        Mockito.when(paymentMethodService.isSessionValid(paymentMethodId, orderId, securityToken))
                .thenReturn(Mono.just(transactionId));

        SessionGetTransactionIdResponseDto expected = new SessionGetTransactionIdResponseDto()
                .transactionId(transactionId);
        webClient
                .get()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{orderId}/transactionId")
                                .build(paymentMethodId, orderId)
                )
                .headers(h -> h.setBearerAuth(securityToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SessionGetTransactionIdResponseDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturn404ForInvalidSession() {
        String paymentMethodId = UUID.randomUUID().toString();
        String orderId = "orderId";
        String securityToken = "securityToken";

        Mockito.when(paymentMethodService.isSessionValid(paymentMethodId, orderId, securityToken))
                .thenReturn(Mono.error(new InvalidSessionException(orderId)));

        ProblemJsonDto expected = new ProblemJsonDto().status(409).title("Invalid session").detail("Invalid session");

        webClient
                .get()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{orderId}/transactionId")
                                .build(paymentMethodId, orderId)
                )
                .headers(h -> h.setBearerAuth(securityToken))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturn404ForSessionNotFound() {
        String paymentMethodId = UUID.randomUUID().toString();
        String orderId = "orderId";
        String securityToken = "securityToken";

        Mockito.when(paymentMethodService.isSessionValid(paymentMethodId, orderId, securityToken))
                .thenReturn(Mono.error(new OrderIdNotFoundException(orderId)));

        ProblemJsonDto expected = new ProblemJsonDto().status(404).title("Not found").detail("Order id not found");

        webClient
                .get()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{orderId}/transactionId")
                                .build(paymentMethodId, orderId)
                )
                .headers(h -> h.setBearerAuth(securityToken))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnErrorOnSessionAlreadyAssociatedError() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(
                        new SessionAlreadyAssociatedToTransaction(
                                "sessionId",
                                "oldTransactionId",
                                "requestedTransactionId"
                        )
                );

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }
}
