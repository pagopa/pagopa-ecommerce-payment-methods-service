package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class BundleOptions {

    private BundleOptions() {
    }

    public static BundleOptionDto removeDuplicatePsp(
                                                     BundleOptionDto optionDto
    ) {
        optionDto.setBundleOptions(
                Optional.ofNullable(optionDto.getBundleOptions())
                        .map(
                                transfers -> transfers.stream().filter(distinctBy(TransferDto::getIdPsp))
                                        .toList()
                        )
                        .orElse(List.of())
        );
        return optionDto;
    }

    public static it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto removeDuplicatePspV2(
                                                                                                it.pagopa.generated.ecommerce.gec.v2.dto.BundleOptionDto optionDto
    ) {
        optionDto.setBundleOptions(
                Optional.ofNullable(optionDto.getBundleOptions())
                        .map(
                                transfers -> transfers.stream().filter(
                                        distinctBy(it.pagopa.generated.ecommerce.gec.v2.dto.TransferDto::getIdPsp)
                                ).toList()
                        )
                        .orElse(List.of())
        );
        return optionDto;
    }

    public static <T> Predicate<T> distinctBy(Function<? super T, ?> f) {
        final Set<Object> objects = new HashSet<>();
        return t -> objects.add(f.apply(t));
    }
}
