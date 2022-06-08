package it.pagopa.ecommerce.payment.instruments.task;

import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentService;
import it.pagopa.ecommerce.payment.instruments.application.PspService;
import it.pagopa.ecommerce.payment.instruments.client.ApiConfigClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class ScheduledPSPUpdate {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    @Autowired
    private ApiConfigClient apiConfigClient;
    @Autowired
    private PspService pspService;
    @Scheduled(cron = "${apiConfig.psp.update.cronString}")
    public void updatePSPs(){
        log.info("Starting PSPs scheduled update. Time: {}", dateFormat.format(new Date()));
        apiConfigClient.getPSPs().subscribe(
                instruments -> pspService.updatePSPs(instruments),
                error -> log.error("[ScheduledPSPUpdate] Error: " + error)
        );
    }
}
