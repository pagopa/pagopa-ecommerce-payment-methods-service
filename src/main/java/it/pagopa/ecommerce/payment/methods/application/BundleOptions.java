package it.pagopa.ecommerce.payment.methods.application;

import it.pagopa.generated.ecommerce.gec.v1.dto.BundleOptionDto;
import it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class BundleOptions {

    private BundleOptions() {
    }

    public static BundleOptionDto removeDuplicatePsp(
                                                     BundleOptionDto optionDto
    ) {
        optionDto.setBundleOptions(removeDuplicatePsp(optionDto.getBundleOptions()));
        return optionDto;
    }

    private static List<TransferDto> removeDuplicatePsp(
                                                        List<it.pagopa.generated.ecommerce.gec.v1.dto.TransferDto> transfers
    ) {
        Set<String> idPsps = new HashSet<>();
        return Optional.ofNullable(transfers)
                .map(
                        transferDtos -> transferDtos.stream()
                                .filter(t -> {
                                    if (idPsps.contains(t.getIdPsp())) {
                                        return false;
                                    } else {
                                        idPsps.add(t.getIdPsp());
                                        return true;
                                    }
                                })
                                .toList()
                )
                .orElse(List.of());

    }

}
