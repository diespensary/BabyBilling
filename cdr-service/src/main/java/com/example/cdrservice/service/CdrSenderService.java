package com.example.cdrservice.service;

import com.example.cdrservice.entity.CdrRecord;
import com.example.cdrservice.repository.CdrRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CdrSenderService {

    private final CdrGenerationService generator;
    private final CdrRecordRepository cdrRepo;
    private final RabbitTemplate rabbit;

    @Scheduled(fixedDelayString = "${cdr.send.delay}")
    public void send() {
        try {
            for (int i = 0; i < 10; i++) {
                generator.generateNext();
            }
        } catch (IllegalStateException e) { }

        List<CdrRecord> batch = cdrRepo.findTop10ByOrderByStartTimeAsc();
        if (batch.isEmpty()) return;

        StringBuilder csv = new StringBuilder();
        for (CdrRecord r : batch) {
            csv.append(r.getCallType()).append(',')
                    .append(r.getFirstSubscriber()).append(',')
                    .append(r.getSecondSubscriber()).append(',')
                    .append(r.getStartTime()).append(',')
                    .append(r.getEndTime()).append('\n');
        }

        rabbit.convertAndSend("cdr.exchange", "cdr.routingKey", csv.toString());
        cdrRepo.deleteAll(batch);

        if (batch.size() < 10) return;
    }

}
