package it.pagopa.ecommerce.payment.methods.task;

import it.pagopa.ecommerce.payment.methods.application.PspService;
import it.pagopa.ecommerce.payment.methods.client.ApiConfigClient;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class ScheduledPSPUpdate {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    @Autowired
    private ApiConfigClient apiConfigClient;
    @Autowired
    private PspService pspService;
    @Scheduled(cron = "${apiConfig.psp.update.cronString}")
    public void updatePSPs(){
        AtomicReference<Integer> currentPage = new AtomicReference<>(0);
        log.info("Starting PSPs scheduled update. Time: {}", dateFormat.format(new Date()));

        apiConfigClient.getPSPs(0, 50).expand(
                servicesDto -> {
                    if (servicesDto.getPageInfo().getTotalPages().equals(currentPage.get()+1)) {
                        return Mono.empty();
                    }
                    return apiConfigClient.getPSPs(currentPage.updateAndGet(v -> v + 1), 50);
                }
        ).collectList().subscribe(
                methods -> {
                    for(ServicesDto servicesDto: methods) {
                        pspService.updatePSPs(servicesDto);
                    }
                },
                error -> log.error("[ScheduledPSPUpdate] Error: " + error)
        );
    }
}
