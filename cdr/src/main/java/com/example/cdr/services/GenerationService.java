package com.example.cdr.services;

import com.example.cdr.entity.CallEntity;
import com.example.cdr.entity.SubscriberEntity;
import com.example.cdr.exception.DataAlreadyGeneratedException;
import com.example.cdr.repository.CallRepo;
import com.example.cdr.repository.SubsRepo;
import com.example.cdr.task.GenerateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * служебный класс для генерации
 */
@Service
public class GenerationService {
    @Autowired
    SubsRepo subsRepo;
    @Autowired
    CdrService cdrService;
    @Autowired
    CallRepo callRepo;
    Random random = new Random();
    @Value("${nPools}")
    int nPools;

    public int generateCalls(int bot, int top) throws DataAlreadyGeneratedException {
        if (callRepo.count() != 0) {
            throw new DataAlreadyGeneratedException("Data Already Generated", HttpStatus.ALREADY_REPORTED);
        }

        List<CallEntity> calls = Collections.synchronizedList(new ArrayList<>());

        List<SubscriberEntity> allSubs = subsRepo.findAll();
        long startLong = LocalDateTime.now().minusYears(1).minusDays(1).toEpochSecond(ZoneOffset.UTC);
        long endLong = LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC);
        int nCalls = random.nextInt(bot, top);
        ExecutorService pool = Executors.newFixedThreadPool(nPools);
        for (int i = 0; i < nPools; i++) {
            if (nCalls % nPools != 0 && i == nPools - 1) {
                pool.execute(new GenerateTask(nCalls % nPools, calls, startLong, endLong, allSubs));
            } else {
                pool.execute(new GenerateTask(nCalls / nPools, calls, startLong, endLong, allSubs));
            }
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }
        calls.sort(Comparator.comparing(CallEntity::getStartCall));

        ConcurrentHashMap<String, List<CallEntity>> cdrMap = new ConcurrentHashMap<>();
        allSubs.forEach(sub -> cdrMap.put(sub.getMsisdn(), new ArrayList<>()));
        ConcurrentHashMap<String, LocalDateTime> lastCalls = new ConcurrentHashMap<>();

        int approvedCalls = 0;
        for (CallEntity call : calls) {
            if (validateCall(lastCalls, call)) {
                continue;
            }

            approvedCalls++;

            //переход через 00:00
            if (call.getStartCall().getDayOfYear() != call.getEndCall().getDayOfYear()) {
                LocalDateTime midnight = call.getEndCall().toLocalDate().atStartOfDay();

                CallEntity beforeMidnight = new CallEntity(
                        null, call.getInitiating(), call.getReceiving(),
                        call.getStartCall(), midnight
                );
                processSingleCall(beforeMidnight, cdrMap);

                CallEntity afterMidnight = new CallEntity(
                        null, call.getInitiating(), call.getReceiving(),
                        midnight, call.getEndCall()
                );
                processSingleCall(afterMidnight, cdrMap);
            } else {
                processSingleCall(call, cdrMap);
            }

            callRepo.saveAndFlush(call);
        }

        return approvedCalls;
    }

    private void processSingleCall(CallEntity call, ConcurrentHashMap<String, List<CallEntity>> cdrMap) {
        cdrMap.computeIfPresent(call.getInitiating().getMsisdn(), (k, v) -> {
            v.add(call);
            return v;
        });

        cdrMap.computeIfPresent(call.getReceiving().getMsisdn(), (k, v) -> {
            v.add(call);
            return v;
        });

        checkAndSendReport(call, cdrMap);
    }

    private void checkAndSendReport(CallEntity call, ConcurrentHashMap<String, List<CallEntity>> cdrMap) {
        String initMsisdn = call.getInitiating().getMsisdn();
        List<CallEntity> initList = cdrMap.get(initMsisdn);

        if (initList != null && initList.size() >= 10) {
            cdrService.sendCdrReport(initMsisdn, new ArrayList<>(initList));
            initList.clear();
        }

        String recMsisdn = call.getReceiving().getMsisdn();
        List<CallEntity> recList = cdrMap.get(recMsisdn);

        if (recList != null && recList.size() >= 10) {
            cdrService.sendCdrReport(recMsisdn, new ArrayList<>(recList));
            recList.clear();
        }
    }

    boolean validateCall(ConcurrentHashMap<String, LocalDateTime> lastCalls, CallEntity call) {
        if (!lastCalls.getOrDefault(call.getInitiating().getMsisdn(), call.getStartCall().minusDays(1)).isBefore(call.getStartCall())) {
            return true;
        }
        if (!lastCalls.getOrDefault(call.getReceiving().getMsisdn(), call.getStartCall().minusDays(1)).isBefore(call.getStartCall())) {
            return true;
        }

        lastCalls.put(call.getReceiving().getMsisdn(), call.getEndCall());
        lastCalls.put(call.getInitiating().getMsisdn(), call.getEndCall());
        return false;
    }
}


