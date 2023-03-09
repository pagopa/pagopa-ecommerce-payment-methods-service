package it.pagopa.ecommerce.payment.methods.service;

import it.pagopa.ecommerce.payment.methods.application.FeeService;
import it.pagopa.ecommerce.payment.methods.application.PaymentMethodService;
import it.pagopa.ecommerce.payment.methods.client.AfmClient;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentOptionDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.data.util.Pair;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class FeeServiceTests {

    @Mock
    private AfmClient afmClient;

    @Mock
    private it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private FeeService feeService;

    @Test
    void shouldRetrieveFee() {
        PaymentOptionDto paymentOptionDtoRequest = TestUtil.getPaymentOptionRequest();
        paymentOptionDtoRequest.setPaymentMethod("CP");
        BundleOptionDto gecResponse = TestUtil.getBundleOptionDtoClientResponse();

        Mockito.when(paymentMethodRepository.findByPaymentMethodStatus(PaymentMethodStatusEnum.ENABLED.getCode()))
                .thenReturn(
                        Flux.just(
                                new PaymentMethodDocument(
                                        UUID.randomUUID().toString(),
                                        "Carte",
                                        "",
                                        PaymentMethodStatusEnum.ENABLED.getCode(),
                                        "asset",
                                        List.of(Pair.of(0L, 100L)),
                                        "CP"
                                )
                        )
                );
        Mockito.when(afmClient.getFees(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(gecResponse));

        feeService = new FeeService(afmClient, paymentMethodRepository);

        it.pagopa.ecommerce.payment.methods.server.model.BundleOptionDto serviceResponse = feeService
                .computeFee(Mono.just(paymentOptionDtoRequest), null).block();
        assertEquals(gecResponse.getBundleOptions().size(), serviceResponse.getBundleOptions().size());
    }

    @Test
    void shouldFilterDisabledPSP() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        PaymentMethodDocument paymentMethodDocument = TestUtil.getTestPaymentDoc(paymentMethod);
        // Only CP method is enabled
        paymentMethodDocument.setPaymentMethodTypeCode("CP");

        List<TransferDto> transferDtos = TestUtil.getBundleOptionDtoClientResponse().getBundleOptions();

        // This should be filtered out -> PPAY is not enabled
        transferDtos.get(0).setPaymentMethod("PPAY");

        Mockito.when(paymentMethodRepository.findByPaymentMethodStatus(PaymentMethodStatusEnum.ENABLED.getCode()))
                .thenReturn(Flux.just(paymentMethodDocument));

        List<TransferDto> filteredTransfers = feeService.removeDisabledPsp(transferDtos).block();

        assertEquals(0, filteredTransfers.size());

    }
}
