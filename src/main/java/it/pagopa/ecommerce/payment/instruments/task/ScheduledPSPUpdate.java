package it.pagopa.ecommerce.payment.instruments.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class ScheduledPSPUpdate {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0 0 * * * *")
    public void updatePSPs(){
        log.info("Starting PSPs scheduled update. Time: {}", dateFormat.format(new Date()));
    }
}
