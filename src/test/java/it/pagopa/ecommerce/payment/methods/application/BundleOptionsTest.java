package it.pagopa.ecommerce.payment.methods.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BundleOptionsTest {

    @ParameterizedTest
    @MethodSource("duplicatedPsps")
    void shouldRemoveDuplicatePsps(List<String> psps) {
        final var options = new BundleOptionDto()
                .belowThreshold(false)
                .bundleOptions(
                        psps.stream().map(it -> new TransferDto().idPsp(it)).toList()
                );
        final var result = BundleOptions.removeDuplicatePsp(options);
        assertEquals(3, result.getBundleOptions().size());
        assertThat(result.getBundleOptions().stream().map(TransferDto::getIdPsp)).hasSameElementsAs(
                List.of("psp1", "psp2", "psp3")
        );
    }

    @Nested
    class V2 {
        @ParameterizedTest
        @MethodSource("it.pagopa.ecommerce.payment.methods.application.BundleOptionsTest#duplicatedPsps")
        void shouldRemoveDuplicatePsps(List<String> psps) {
            final var options = new it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto()
                    .belowThreshold(false)
                    .bundleOptions(
                            psps.stream()
                                    .map(it -> new it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto().idPsp(it))
                                    .toList()
                    );
            final var result = BundleOptions.removeDuplicatePspV2(options);
            assertEquals(3, result.getBundleOptions().size());
            assertThat(
                    result.getBundleOptions().stream()
                            .map(it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto::getIdPsp)
            ).hasSameElementsAs(
                    List.of("psp1", "psp2", "psp3")
            );
        }
    }

    public static Stream<Arguments> duplicatedPsps() {
        return Stream.of(
                Arguments.of(List.of("psp1", "psp1", "psp2", "psp1", "psp2", "psp3")),
                Arguments.of(List.of("psp1", "psp2", "psp3")),
                Arguments.of(List.of("psp1", "psp2", "psp3", "psp3"))
        );
    }
}
