package it.pagopa.ecommerce.payment.methods.service.v2;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.domain.Claims;
import it.pagopa.ecommerce.commons.exceptions.JWTTokenGenerationException;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.FieldsDto;
import it.pagopa.ecommerce.commons.utils.JwtTokenUtils;
import it.pagopa.ecommerce.commons.utils.UniqueIdUtils;
import it.pagopa.ecommerce.payment.methods.application.v2.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.config.SecretsConfigurations;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.methods.config.SessionUrlConfig;
import it.pagopa.ecommerce.payment.methods.exception.NoBundleFoundException;
import it.pagopa.ecommerce.payment.methods.infrastructure.NpgSessionsTemplateWrapper;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.ecommerce.payment.methods.v2.server.model.*;
import it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import io.vavr.control.Either;

import javax.crypto.SecretKey;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTests {

    private static final String STRONG_KEY = "ODMzNUZBNTZENDg3NTYyREUyNDhGNDdCRUZDNzI3NDMzMzQwNTFEREZGQ0MyQzA5Mjc1RjY2NTQ1NDk5MDMxNzU5NDc0NUVFMTdDMDhGNzk4Q0Q3RENFMEJBODE1NURDREExNEY2Mzk4QzFEMTU0NTExNjUyMEExMzMwMTdDMDk";

    private final AfmClient afmClient = mock(AfmClient.class);

    private final NpgClient npgClient = mock(NpgClient.class);

    private final PaymentMethodRepository paymentMethodRepository = mock(PaymentMethodRepository.class);

    private final PaymentMethodFactory paymentMethodFactory = mock(PaymentMethodFactory.class);

    private final SessionUrlConfig sessionUrlConfig = new SessionUrlConfig(
            URI.create("http://localhost:1234"),
            "/esito",
            "/annulla",
            "https://localhost/sessions/{orderId}/outcomes?sessionToken={sessionToken}"
    );

    private final String npgDefaultApiKey = UUID.randomUUID().toString();

    private final NpgSessionsTemplateWrapper npgSessionsTemplateWrapper = mock(NpgSessionsTemplateWrapper.class);

    private final UniqueIdUtils uniqueIdUtils = mock(UniqueIdUtils.class);

    private final SecretKey jwtSecretKey = new SecretsConfigurations().npgJwtSigningKey(STRONG_KEY);

    private final JwtTokenUtils jwtTokenUtils = mock(JwtTokenUtils.class);

    private final PaymentMethodService paymentMethodService = new PaymentMethodService(
            afmClient,
            paymentMethodRepository,
            paymentMethodFactory,
            npgClient,
            sessionUrlConfig,
            npgSessionsTemplateWrapper,
            npgDefaultApiKey,
            uniqueIdUtils,
            jwtSecretKey,
            900,
            jwtTokenUtils
    );

    @Test
    void shouldRetrieveFeeForMultiplePaymentNotice() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
        assertEquals(PAYMENT_METHOD_TEST.getPaymentMethodName(), serviceResponse.getPaymentMethodName());
        assertEquals(
                PAYMENT_METHOD_TEST.getPaymentMethodDescription(),
                serviceResponse.getPaymentMethodDescription()
        );
    }

    @Test
    void shouldRetrieveFeeForMultiplePaymentNoticeWithoutPspList() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        calculateFeeRequestDto.setIdPspList(null);
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));

        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundles().size());
    }

    @Test
    void shouldRetrieveFeeForMultiplePaymentNoticeWithPspWithNullPaymentType() {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        calculateFeeRequestDto.setIdPspList(null);
        final var gecResponse = TestUtil.V2.getBundleOptionWithAnyValueDtoClientResponse();
        String paymentTypeCode = "CP";

        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));

        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        CalculateFeeResponseDto serviceResponse = paymentMethodService
                .computeFee(calculateFeeRequestDto, paymentMethodId, null).block();
        assertEquals(paymentTypeCode, serviceResponse.getBundles().get(0).getPaymentMethod());
    }

    public static Stream<Arguments> gecInvalidTransferDtoSource() {
        return Stream.of(Arguments.of(List.<TransferDto>of()), Arguments.of((List<TransferDto>) null));
    }

    @ParameterizedTest
    @MethodSource("gecInvalidTransferDtoSource")
    void shouldReturnNoBundleFoundExceptionForNoBundleReturnedByGec(List<TransferDto> invalidTransferDto) {
        final var paymentMethodId = UUID.randomUUID().toString();
        final var calculateFeeRequestDto = TestUtil.V2.getMultiNoticeFeesRequest();
        final var gecResponse = TestUtil.V2.getBundleOptionDtoClientResponse();
        gecResponse.setBundleOptions(invalidTransferDto);
        Mockito.when(paymentMethodRepository.findById(paymentMethodId))
                .thenReturn(Mono.just(PAYMENT_METHOD_TEST));
        Mockito.when(afmClient.getFeesForNotices(any(), any(), Mockito.anyBoolean()))
                .thenReturn(Mono.just(gecResponse));

        StepVerifier.create(
                paymentMethodService
                        .computeFee(calculateFeeRequestDto, paymentMethodId, null)
        )
                .expectError(NoBundleFoundException.class)
                .verify();
    }

    private final PaymentMethodDocument PAYMENT_METHOD_TEST = new PaymentMethodDocument(
            UUID.randomUUID().toString(),
            NpgClient.PaymentMethod.CARDS.serviceName,
            "Description",
            PaymentMethodStatusEnum.ENABLED.getCode(),
            "asset",
            List.of(Pair.of(0L, 100L)),
            "CP",
            PaymentMethodRequestDto.ClientIdEnum.CHECKOUT.getValue(),
            PaymentMethodManagementTypeDto.ONBOARDABLE.getValue(),
            null
    );
}
