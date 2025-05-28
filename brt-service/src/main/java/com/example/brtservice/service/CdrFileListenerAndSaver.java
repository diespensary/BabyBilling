package com.example.brtservice.service;

import com.example.brtservice.dto.CdrRecordDto;
import com.example.brtservice.entity.CdrRecord;
import com.example.brtservice.mapper.CdrRecordMapper;
import com.example.brtservice.parser.CdrFileParser;
import com.example.brtservice.repository.AbonentRepository;
import com.example.brtservice.repository.CdrRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CdrFileListenerAndSaver {
    private static final Logger log = LoggerFactory.getLogger(CdrFileListenerAndSaver.class);

    private final CdrRecordRepository cdrRepo;
    private final AbonentRepository abonentRepo;

    @RabbitListener(queues = "cdr.queue")
    public void receive(String csvPayload) {
        // 1) Парсим CSV в DTO
        List<CdrRecordDto> dtos = CdrFileParser.parse(csvPayload);

        // 2) Фильтруем по абонентам «Romashka»
        List<CdrRecordDto> filteredDtos = dtos.stream()
                .filter(dto ->
                        abonentRepo.existsByMsisdn(dto.getFirstSubscriber()) ||
                                abonentRepo.existsByMsisdn(dto.getSecondSubscriber())
                )
                .collect(Collectors.toList());

        if (filteredDtos.isEmpty()) {
            log.info("Нет записей с абонентами Romashka, пропускаем пакет.");
            return;
        }

        // 3) Мапим DTO → Entity и сохраняем
        List<CdrRecord> cdrRecords = filteredDtos.stream()
                .map(CdrRecordMapper::toEntity)
                .collect(Collectors.toList());

        cdrRepo.saveAll(cdrRecords);
        cdrRecords.forEach(e -> log.info("Сохранён CDR: {}", e));
    }
}
