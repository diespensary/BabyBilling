package com.example.brtservice.service;

import com.example.brtservice.entity.Abonent;
import com.example.brtservice.entity.CdrRecord;
import com.example.brtservice.entity.TariffBalance;
import com.example.brtservice.repository.AbonentRepository;
import com.example.brtservice.repository.CdrRecordRepository;
import com.example.brtservice.repository.TariffBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutgoingCdrSenderService {

    private final CdrRecordRepository     cdrRepo;
    private final AbonentRepository       abonentRepo;
    private final TariffBalanceRepository balanceRepo;
    private final RabbitTemplate          rabbit;

    /**
     * По расписанию формируем CSV из до 10 необработанных исходящих записей
     * и отправляем в HRS, после чего помечаем их processed = true.
     */
    @Scheduled(fixedDelayString = "${billing.send.delay}")
    public void sendBillingBatch() {
        // 1) Выбираем до 10 необработанных исходящих CDR
        List<CdrRecord> batch = cdrRepo
                .findTop10ByCallTypeAndProcessedFalseOrderByStartTimeAsc("01");
        if (batch.isEmpty()) {
            return;
        }

        // 2) Формируем CSV
        StringBuilder csv = new StringBuilder();
        for (CdrRecord rec : batch) {
            Abonent ab = abonentRepo
                    .findByMsisdn(rec.getFirstSubscriber())
                    .orElseThrow(() -> new IllegalStateException(
                            "Абонент не найден: " + rec.getFirstSubscriber()));

            long talkTime = rec.getTalkTimeSeconds();

            // Флаг: является ли secondSubscriber абонентом "Romashka"
            boolean secondIsRomashka = abonentRepo.existsByMsisdn(rec.getSecondSubscriber());

            if (ab.getTariffId() == 11) {
                // Вариант 1: только MSISDN и длительность
                csv.append(ab.getMsisdn())
                        .append(',')
                        .append("11")
                        .append(',')
                        .append(talkTime).append(',')
                        .append(secondIsRomashka)
                        .append('\n');
            } else if (ab.getTariffId() == 12) {
                // Вариант 2: + остаток минут + дата последнего списания
                TariffBalance tb = balanceRepo
                        .findByAbonentId(ab.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "TariffBalance не найден для абонента " + ab.getId()));

                csv.append(ab.getMsisdn()).append(',')
                        .append("12").append(',')
                        .append(talkTime).append(',')
                        .append(tb.getTariffBalance()).append(',')
                        .append(tb.getDateLastBilling()).append(',')
                        .append(rec.getStartTime()).append(',')
                        .append(secondIsRomashka)
                        .append('\n');
            }
        }

        // 3) Отправляем CSV в HRS
        rabbit.convertAndSend("hrs.exchange", "hrs.routingKey", csv.toString());

        // 4) Помечаем записи как обработанные
        batch.forEach(r -> r.setProcessed(true));
        cdrRepo.saveAll(batch);
    }
}