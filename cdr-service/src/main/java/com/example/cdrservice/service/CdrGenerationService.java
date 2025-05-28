package com.example.cdrservice.service;

import com.example.cdrservice.entity.Subscriber;
import com.example.cdrservice.entity.CdrRecord;
import com.example.cdrservice.repository.SubscriberRepository;
import com.example.cdrservice.repository.CdrRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CdrGenerationService {
    private final SubscriberRepository subRepo;
    private final CdrRecordRepository cdrRepo;

    private Subscriber prevFirstSubscriber;
    private Subscriber prevSecondSubscriber;
    private LocalDateTime prevStartTime;
    private Duration prevDuration;

    private static final LocalDateTime BORDER = LocalDateTime.of(2026,1,1,0,0);

    public CdrGenerationService(SubscriberRepository subRepo,
                                CdrRecordRepository cdrRepo) {
        this.subRepo = subRepo;
        this.cdrRepo = cdrRepo;
        this.prevStartTime = LocalDateTime.of(2025,1,1,0,0);
        this.prevDuration = Duration.ZERO;
    }

    /**
     * Генерирует и сохраняет CDR-записи (1 или 2 при зеркалировании),
     * разбивая по полуночи при необходимости.
     */
    @Transactional
    public void generateNext() {
        List<Subscriber> all = subRepo.findAll();
        List<Subscriber> rom = all.stream()
                .filter(s -> "Romashka".equals(s.getOperator()))
                .toList();

        // 1) интервал до следующего вызова
        Duration interval = randomBetween(Duration.ofSeconds(1), Duration.ofDays(7));
        LocalDateTime start = prevStartTime.plus(interval);

        // 2) проверка границы периода
        if (start.isAfter(BORDER)) {
            throw new IllegalStateException("Generation period ended");
        }

        // 3) выбор первого (только Romashka) и второго (любой, но != первому)
        Subscriber first, second;
        // Для конкурирующих вызовов не допускать повторений
        if (interval.compareTo(prevDuration) < 0) {
            // оба не равны предыдущим
            var allowedFirst = rom.stream()
                    .filter(s -> !s.equals(prevFirstSubscriber) && !s.equals(prevSecondSubscriber))
                    .toList();
            first = random(allowedFirst);

            var allowedSecond = all.stream()
                    .filter(s -> !s.equals(prevFirstSubscriber)
                            && !s.equals(prevSecondSubscriber)
                            && !s.equals(first))
                    .toList();
            second = random(allowedSecond);
        } else {
            // можно брать любые
            first = random(rom);

            var allowedSecond = all.stream()
                    .filter(s -> !s.equals(first))
                    .toList();
            second = random(allowedSecond);
        }

        // 4) случайный тип основного вызова и зеркалирование только если оба Romashka
        boolean isPrimary01 = ThreadLocalRandom.current().nextBoolean();
        String primaryType = isPrimary01 ? "01" : "02";
        String mirrorType  = isPrimary01 ? "02" : "01";
        boolean generateMirror = "Romashka".equals(first.getOperator())
                && "Romashka".equals(second.getOperator());

        // 5) длительность и время конца
        Duration duration = randomBetween(Duration.ofSeconds(1), Duration.ofMinutes(30));
        LocalDateTime end = start.plus(duration);

        // обновляем prev-значения
        prevStartTime      = start;
        prevDuration       = duration;
        prevFirstSubscriber = first;
        prevSecondSubscriber = second;

        // 6) сохраняем записи, разбивая по полуночи
        if (start.toLocalDate().equals(end.toLocalDate())) {
            saveRecord(first, second, start, end, primaryType);
            if (generateMirror) {
                saveRecord(second, first, start, end, mirrorType);
            }
        } else {
            // часть до полуночи
            LocalDateTime firstEnd = start.toLocalDate().atTime(23,59,59);
            saveRecord(first, second, start, firstEnd, primaryType);
            if (generateMirror) {
                saveRecord(second, first, start, firstEnd, mirrorType);
            }
            // часть после
            LocalDateTime secondStart = firstEnd.plusSeconds(1);
            saveRecord(first, second, secondStart, end, primaryType);
            if (generateMirror) {
                saveRecord(second, first, secondStart, end, mirrorType);
            }
        }
    }

    private void saveRecord(Subscriber firstSubscriber,
                            Subscriber secondSubscriber,
                            LocalDateTime s,
                            LocalDateTime e,
                            String callType) {
        CdrRecord r = new CdrRecord();
        r.setCallType(callType);
        r.setFirstSubscriber(firstSubscriber.getMsisdn());
        r.setSecondSubscriber(secondSubscriber.getMsisdn());
        r.setStartTime(s);
        r.setEndTime(e);
        cdrRepo.save(r);
    }

    private <T> T random(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private Duration randomBetween(Duration min, Duration max) {
        long sec = ThreadLocalRandom.current()
                .nextLong(min.getSeconds(), max.getSeconds() + 1);
        return Duration.ofSeconds(sec);
    }
}
