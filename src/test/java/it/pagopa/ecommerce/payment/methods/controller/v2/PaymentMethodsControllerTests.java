package it.pagopa.ecommerce.payment.methods.controller.v2;

import io.opentelemetry.api.trace.Tracer;
import it.pagopa.ecommerce.commons.domain.TransactionId;
import it.pagopa.ecommerce.payment.methods.application.v2.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.exception.AfmResponseException;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodNotFoundException;
import it.pagopa.ecommerce.payment.methods.server.model.ProblemJsonDto;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeRequestDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.CalculateFeeResponseDto;
import it.pagopa.ecommerce.payment.methods.v2.server.model.SessionGetTransactionIdResponseDto;
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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@WebFluxTest(it.pagopa.ecommerce.payment.methods.controller.v2.PaymentMethodsController.class)
@TestPropertySource(locations = "classpath:application.test.properties")
class PaymentMethodsControllerTests {

    @MockBean
    private PaymentMethodService paymentMethodService;

    @InjectMocks
    private PaymentMethodsController paymentMethodsController;

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private Tracer tracer;

    @Test
    void shouldGetFees() {
        final String paymentMethodId = UUID.randomUUID().toString();
        final CalculateFeeRequestDto requestBody = TestUtil.V2.getMultiNoticeFeesRequest();
        final CalculateFeeResponseDto serviceResponse = TestUtil.V2
                .getCalculateFeeResponseFromClientResponse(TestUtil.getBundleOptionDtoClientResponse());
        Mockito.when(paymentMethodService.computeFee(any(), any(), any()))
                .thenReturn(Mono.just(serviceResponse));

        webClient
                .post()
                .uri("/v2/payment-methods/" + paymentMethodId + "/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CalculateFeeResponseDto.class)
                .isEqualTo(serviceResponse);
    }

    @Test
    void shouldReturn400WithEmptyPaymentNotices() {
        final String paymentMethodId = UUID.randomUUID().toString();
        final CalculateFeeRequestDto requestBody = TestUtil.V2.getMultiNoticeFeesRequest().paymentNotices(
                List.of()
        );

        webClient
                .post()
                .uri("/v2/payment-methods/" + paymentMethodId + "/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldReturn404ForNoBundleReturned() {
        String paymentMethodId = UUID.randomUUID().toString();
        CalculateFeeRequestDto requestBody = TestUtil.V2.getMultiNoticeFeesRequest();
        Mockito.when(paymentMethodService.computeFee(any(), any(), any()))
                .thenReturn(Mono.error(new NoBundleFoundException("paymentMethodId", 100, "CHECKOUT")));
        ProblemJsonDto expected = new ProblemJsonDto().status(404).title("Not found").detail(
                "No bundle found for payment method with id: [paymentMethodId] and transaction amount: [100] for touch point: [CHECKOUT]"
        );
        webClient
                .post()
                .uri("/v2/payment-methods/" + paymentMethodId + "/fees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ProblemJsonDto.class)
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnResponseEntityWithNotFound() {
        ResponseEntity<ProblemJsonDto> responseEntity = paymentMethodsController
                .errorHandler(new PaymentMethodNotFoundException("paymentMethodId"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Payment method not found", responseEntity.getBody().getDetail());
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
        TransactionId transactionId = new TransactionId(UUID.randomUUID());

        Mockito.when(paymentMethodService.isSessionValid(any(), any(), any()))
                .thenReturn(Mono.just(transactionId));

        SessionGetTransactionIdResponseDto expected = new SessionGetTransactionIdResponseDto()
                .base64EncodedTransactionId(transactionId.base64())
                .transactionId(transactionId.value());
        webClient
                .get()
                .uri(
                        builder -> builder
                                .path("/v2/payment-methods/{paymentMethodId}/sessions/{orderId}/transactionId")
                                .build(paymentMethodId, orderId)
                )
                .headers(h -> h.setBearerAuth(securityToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SessionGetTransactionIdResponseDto.class)
                .isEqualTo(expected);
        verify(paymentMethodService, times(1))
                .isSessionValid(paymentMethodId, orderId, securityToken);
    }

}
