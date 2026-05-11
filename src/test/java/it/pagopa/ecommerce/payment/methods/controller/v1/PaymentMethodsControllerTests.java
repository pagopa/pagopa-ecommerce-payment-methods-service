package it.pagopa.ecommerce.payment.methods.controller.v1;

import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.domain.v2.TransactionId;
import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.payment.methods.application.v1.PaymentMethodService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PaymentMethodsController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
class PaymentMethodsControllerTests {

    @InjectMocks
    private PaymentMethodsController paymentMethodsController = new PaymentMethodsController();

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    @MockitoBean
    private Tracer tracer;

    @Test
    void shouldCreateNewMethodForCheckout() {
        PaymentMethodRequestDto paymentMethodRequestDto = TestUtil.getPaymentMethodRequestForCheckout();

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

        PaymentMethodResponseDto methodResponse = TestUtil.getPaymentMethodResponse(paymentMethod);

        Mockito.when(
                paymentMethodService
                        .createPaymentMethod(any())
        )
                .thenReturn(Mono.just(paymentMethod));

        webClient
                .post().uri("/payment-methods")
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentMethodRequestDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentMethodResponseDto.class)
                .isEqualTo(methodResponse);
    }

    @Test
    void shouldReturn401ForNewMethodWithInvalidApiKey() {
        PaymentMethodRequestDto paymentMethodRequestDto = TestUtil.getPaymentMethodRequestForCheckout();

        webClient
                .post().uri("/payment-methods")
                .header("x-api-key", "not-valid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentMethodRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForNewMethodWithMissingApiKey() {
        PaymentMethodRequestDto paymentMethodRequestDto = TestUtil.getPaymentMethodRequestForCheckout();

        webClient
                .post().uri("/payment-methods")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentMethodRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldCreateNewMethodForIO() {
        PaymentMethodRequestDto paymentMethodRequestDto = TestUtil.getPaymentMethodRequestForIO();

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdIO = TestUtil.getClientIdIO();

        PaymentMethodResponseDto methodResponse = TestUtil.getPaymentMethodResponse(paymentMethod);

        Mockito.when(
                paymentMethodService
                        .createPaymentMethod(any())
        )
                .thenReturn(Mono.just(paymentMethod));

        webClient
                .post().uri("/payment-methods")
                .header("x-api-key", "secondary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentMethodRequestDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentMethodResponseDto.class)
                .isEqualTo(methodResponse);
    }

    @Test
    void shouldGetAllMethodsForCheckout() {

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdCheckout = TestUtil.getClientIdCheckout();

        Mockito.when(
                paymentMethodService
                        .retrievePaymentMethods((int) TestUtil.getTestAmount(), clientIdCheckout.getValue(), null)
        ).thenReturn(
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
                .header("x-client-id", TestUtil.getClientIdCheckout().toString())
                .header("x-api-key", "primary-key")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentMethodsResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    void shouldReturn401ForGetAllMethodsWithInvalidApiKey() {
        webClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/payment-methods")
                                .queryParam("amount", TestUtil.getTestAmount())
                                .build()
                )
                .header("x-client-id", TestUtil.getClientIdCheckout().toString())
                .header("x-api-key", "not-valid-key")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForGetAllMethodsWithMissingApiKey() {
        webClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/payment-methods")
                                .queryParam("amount", TestUtil.getTestAmount())
                                .build()
                )
                .header("x-client-id", TestUtil.getClientIdCheckout().toString())
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldGetAllMethodsForIo() {

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdIO = TestUtil.getClientIdIO();

        Mockito.when(
                paymentMethodService.retrievePaymentMethods((int) TestUtil.getTestAmount(), clientIdIO.getValue(), null)
        )
                .thenReturn(
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
                .header("x-client-id", TestUtil.getClientIdIO().toString())
                .header("x-api-key", "primary-key")
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

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

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
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patchRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentMethodResponseDto.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldReturn401ForPatchPaymentMethodWithInvalidApiKey() {

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

        PaymentMethodRequestDto patchRequest = new PaymentMethodRequestDto()
                .status(PaymentMethodStatusDto.ENABLED);

        webClient
                .patch().uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .header("x-api-key", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patchRequest)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForPatchPaymentMethodWithMissingApiKey() {

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

        PaymentMethodRequestDto patchRequest = new PaymentMethodRequestDto()
                .status(PaymentMethodStatusDto.ENABLED);

        webClient
                .patch().uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patchRequest)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldGetAMethodForCheckout() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdCheckout = TestUtil.getClientIdCheckout();

        Mockito.when(
                paymentMethodService.retrievePaymentMethodById(
                        paymentMethod.getPaymentMethodID().value().toString(),
                        clientIdCheckout.getValue()
                )
        ).thenReturn(Mono.just(paymentMethod));

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .header("x-client-id", TestUtil.getClientIdCheckout().toString())
                .header("x-api-key", "primary-key")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PaymentMethodResponseDto.class)
                .hasSize(1)
                .contains(expectedResult);
    }

    @Test
    void shouldReturn401ForGetAMethodWithInvalidApiKey() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .header("x-client-id", TestUtil.getClientIdCheckout().toString())
                .header("x-api-key", "birra-key")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForGetAMethodWithMissingApiKey() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .header("x-client-id", TestUtil.getClientIdCheckout().toString())
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldGetAMethodForIo() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdIO = TestUtil.getClientIdIO();

        Mockito.when(
                paymentMethodService.retrievePaymentMethodById(
                        paymentMethod.getPaymentMethodID().value().toString(),
                        clientIdIO.getValue()
                )
        ).thenReturn(Mono.just(paymentMethod));

        PaymentMethodResponseDto expectedResult = TestUtil.getPaymentMethodResponse(paymentMethod);

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethod.getPaymentMethodID().value())
                .header("x-client-id", TestUtil.getClientIdIO().toString())
                .header("x-api-key", "primary-key")
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
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CalculateFeeResponseDto.class)
                .isEqualTo(serviceResponse);
    }

    @Test
    void shouldReturn401ForGetFeesWithInvalidApiKey() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto requestBody = TestUtil.getCalculateFeeRequest();

        webClient
                .post()
                .uri("/payment-methods/" + paymentMethodId + "/fees")
                .header("x-api-key", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForGetFeesWithMissingApiKey() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto requestBody = TestUtil.getCalculateFeeRequest();

        webClient
                .post()
                .uri("/payment-methods/" + paymentMethodId + "/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturnResponseWithSuccessfulUpdate() {
        String paymentMethodId = UUID.randomUUID().toString();
        PatchSessionRequestDto requestBody = TestUtil.patchSessionRequest();
        String newTransactionId = requestBody.getTransactionId();
        String correlationId = UUID.randomUUID().toString();
        NpgSessionDocument originalSession = TestUtil
                .npgSessionDocument("orderId", correlationId, "sessionId", false, null);
        NpgSessionDocument updatedDocument = TestUtil.patchSessionResponse(originalSession, newTransactionId);

        Mockito.when(paymentMethodService.updateSession(paymentMethodId, originalSession.orderId(), requestBody))
                .thenReturn(Mono.just(updatedDocument));

        webClient
                .patch()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}")
                                .build(paymentMethodId, originalSession.orderId())
                )
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    void shouldReturn401ForSessionUpdateWithInvalidKey() {
        String paymentMethodId = UUID.randomUUID().toString();
        PatchSessionRequestDto requestBody = TestUtil.patchSessionRequest();
        String correlationId = UUID.randomUUID().toString();
        NpgSessionDocument originalSession = TestUtil
                .npgSessionDocument("orderId", correlationId, "sessionId", false, null);

        webClient
                .patch()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}")
                                .build(paymentMethodId, originalSession.orderId())
                )
                .header("x-api-key", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForSessionUpdateWithMissingKey() {
        String paymentMethodId = UUID.randomUUID().toString();
        PatchSessionRequestDto requestBody = TestUtil.patchSessionRequest();
        String correlationId = UUID.randomUUID().toString();
        NpgSessionDocument originalSession = TestUtil
                .npgSessionDocument("orderId", correlationId, "sessionId", false, null);

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
                .isUnauthorized();
    }

    @Test
    void shouldReturnResponseWithSuccessfulUpdateOnRetry() {
        String paymentMethodId = UUID.randomUUID().toString();
        PatchSessionRequestDto requestBody = TestUtil.patchSessionRequest();
        String newTransactionId = requestBody.getTransactionId();
        String correlationId = UUID.randomUUID().toString();
        NpgSessionDocument originalSession = TestUtil
                .npgSessionDocument("orderId", correlationId, "sessionId", false, newTransactionId);
        NpgSessionDocument updatedDocument = TestUtil.patchSessionResponse(originalSession, newTransactionId);

        Mockito.when(paymentMethodService.updateSession(paymentMethodId, originalSession.orderId(), requestBody))
                .thenReturn(Mono.just(updatedDocument));

        webClient
                .patch()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}")
                                .build(paymentMethodId, originalSession.orderId())
                )
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    void shouldReturnError409OnOrderIdAndTransactionIdMismatch() {
        String paymentMethodId = UUID.randomUUID().toString();
        PatchSessionRequestDto requestBody = TestUtil.patchSessionRequest();
        String correlationId = UUID.randomUUID().toString();
        NpgSessionDocument originalSession = TestUtil
                .npgSessionDocument("orderId", correlationId, "sessionId", false, "ANOTHER_TRANSACTION_ID");

        Mockito.when(paymentMethodService.updateSession(paymentMethodId, originalSession.orderId(), requestBody))
                .thenReturn(
                        Mono.error(
                                new SessionAlreadyAssociatedToTransaction(
                                        originalSession.orderId(),
                                        originalSession.transactionId(),
                                        requestBody.getTransactionId()
                                )
                        )
                );

        webClient
                .patch()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{sessionId}")
                                .build(paymentMethodId, originalSession.orderId())
                )
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ProblemJsonDto.class);
    }

    @Test
    void shouldPostCreateSession() {
        String paymentMethodId = UUID.randomUUID().toString();
        CreateSessionResponseDto responseDto = TestUtil.createSessionResponseDto(paymentMethodId);
        Mockito.when(paymentMethodService.createSessionForPaymentMethod(any(), any(), any()))
                .thenReturn(Mono.just(responseDto));
        webClient
                .post()
                .uri("/payment-methods/" + paymentMethodId + "/sessions")
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateSessionResponseDto.class)
                .isEqualTo(responseDto);
    }

    @Test
    void shouldReturn401ForCreateSessionWithInvalidKey() {
        String paymentMethodId = UUID.randomUUID().toString();

        webClient
                .post()
                .uri("/payment-methods/" + paymentMethodId + "/sessions")
                .header("x-api-key", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForCreateSessionWithMissingKey() {
        String paymentMethodId = UUID.randomUUID().toString();

        webClient
                .post()
                .uri("/payment-methods/" + paymentMethodId + "/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange()
                .expectStatus()
                .isUnauthorized();
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
                .header("x-api-key", "primary-key")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SessionPaymentMethodResponseDto.class)
                .isEqualTo(response);
    }

    @Test
    void shouldReturn401ForGetCardDataFromSessionIdWithInvalidApiKey() {
        String paymentMethodId = "paymentMethodId";
        String orderId = "orderId";

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethodId + "/sessions/" + orderId)
                .header("x-api-key", "invalid-key")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturn401ForGetCardDataFromSessionIdWithMissingApiKey() {
        String paymentMethodId = "paymentMethodId";
        String orderId = "orderId";

        webClient
                .get()
                .uri("/payment-methods/" + paymentMethodId + "/sessions/" + orderId)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldReturnResponseSessionIdNotFound() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new OrderIdNotFoundException("orderId"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Order id not found", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnResponseUniqueId() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new UniqueIdGenerationException());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Internal system error", responseEntity.getBody().getTitle());
        assertEquals("Error when generating unique id", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnResponseEntityWithNotFound() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new PaymentMethodNotFoundException("paymentMethodId"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Payment method not found", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnResponseEntityWithJwtGenerationError() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new JwtIssuerResponseException(HttpStatus.BAD_GATEWAY, "errror jwt issuer"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Internal server error", responseEntity.getBody().getTitle());
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

    @Test
    void shouldReturnResponseEntityWithNpgError() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(
                        new NpgResponseException(
                                "reason test",
                                Optional.of(HttpStatus.INTERNAL_SERVER_ERROR),
                                new RuntimeException("inner test")
                        )
                );
        assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.getStatusCode());
        assertEquals("reason test", responseEntity.getBody().getDetail());
    }

    @Test
    void shouldReturnTransactionIdForValidSession() {
        String paymentMethodId = UUID.randomUUID().toString();
        String orderId = "orderId";
        String securityToken = "securityToken";
        TransactionId transactionId = new TransactionId(UUID.randomUUID());

        Mockito.when(paymentMethodService.isSessionValid(paymentMethodId, orderId, securityToken))
                .thenReturn(Mono.just(transactionId));

        SessionGetTransactionIdResponseDto expected = new SessionGetTransactionIdResponseDto()
                .transactionId(transactionId.base64());
        webClient
                .get()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{orderId}/transactionId")
                                .build(paymentMethodId, orderId)
                )
                .header("x-api-key", "primary-key")
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
                .header("x-api-key", "primary-key")
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
                .header("x-api-key", "primary-key")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnErrorOnMismatchedSecurityToken() {
        String paymentMethodId = UUID.randomUUID().toString();
        String orderId = "orderId";
        String securityToken = "securityToken";
        String transactionId = "transactionId";

        Mockito.when(paymentMethodService.isSessionValid(eq(paymentMethodId), eq(orderId), any()))
                .thenReturn(Mono.error(new MismatchedSecurityTokenException(orderId, transactionId)));

        ProblemJsonDto expected = new ProblemJsonDto().status(404).title("Not found").detail("Order id not found");

        webClient
                .get()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions/{orderId}/transactionId")
                                .build(paymentMethodId, orderId)
                )
                .headers(h -> h.setBearerAuth(securityToken))
                .header("x-api-key", "primary-key")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturn404ForNoBundleReturned() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto requestBody = TestUtil.getCalculateFeeRequest();
        Mockito.when(paymentMethodService.computeFee(any(), any(), any()))
                .thenReturn(Mono.error(new NoBundleFoundException("paymentMethodId", 100, "CHECKOUT")));
        ProblemJsonDto expected = new ProblemJsonDto().status(404).title("Not found").detail(
                "No bundle found for payment method with id: [paymentMethodId] and transaction amount: [100] for touch point: [CHECKOUT]"
        );
        webClient
                .post()
                .uri("/payment-methods/" + paymentMethodId + "/fees")
                .header("x-api-key", "primary-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnBadGatewayForNpgException() {
        String paymentMethodId = UUID.randomUUID().toString();

        Mockito.when(paymentMethodService.createSessionForPaymentMethod(paymentMethodId, null, null))
                .thenReturn(
                        Mono.error(
                                new NpgResponseException(
                                        "error message",
                                        Optional.of(HttpStatus.INTERNAL_SERVER_ERROR),
                                        new RuntimeException("NPG cause")
                                )
                        )
                );

        webClient
                .post()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions")
                                .build(paymentMethodId)
                )
                .header("x-api-key", "primary-key")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_GATEWAY)
                .expectBody(ProblemJsonDto.class)
                .value(p -> assertEquals(502, p.getStatus()));
    }

    @Test
    void shouldReturnBadRequestForIllegalArgumentException() {
        String paymentMethodId = UUID.randomUUID().toString();

        Mockito.when(paymentMethodService.createSessionForPaymentMethod(paymentMethodId, null, null))
                .thenReturn(
                        Mono.error(
                                new IllegalArgumentException(
                                        "error message"
                                )
                        )
                );

        webClient
                .post()
                .uri(
                        builder -> builder.path("/payment-methods/{paymentMethodId}/sessions")
                                .build(paymentMethodId)
                )
                .header("x-api-key", "primary-key")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody(ProblemJsonDto.class)
                .value(p -> assertEquals(400, p.getStatus()));
    }
}
