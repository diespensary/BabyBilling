package brtApp.Service;

import brtApp.dto.*;
import brtApp.entity.SubscriberEntity;
import brtApp.exception.ClientException;
import brtApp.exception.EntityAlreadyExsistsException;
import brtApp.exception.NotRomashkaException;
import brtApp.exception.TooEarlyForTarifficationException;
import brtApp.client.HrsRest;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import brtApp.repository.SubscriberRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Slf4j
public class SubscriberService {
    @Autowired
    SubscriberRepository subscriberRepository;
    @Autowired
    BalanceChangesService balanceChangesService;
    @Autowired
    HrsRest hrsRest;

    public SubscriberEntity findSubscriberByOwner(CdrDto dto) {
        return subscriberRepository.findByMsisdn(dto.getOwner())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Subscriber not found with MSISDN: " + dto.getOwner()
                ));
    }

    public boolean validateSubscriber(String msisdn) {
        if (!subscriberRepository.findByMsisdn(msisdn).isPresent()) {
            throw new NotRomashkaException("Not Romashka subscriber");
        }
        return true;
    }

    //Изменение баланса после тарификации звонка
    public SubscriberEntity changeBalanceCallTariffication(SubscriberEntity subscriberEntity, HrsRetrieveDto hrsRetrieveDto) {
        subscriberEntity.changeBalance(hrsRetrieveDto.getBalanceChange());
        subscriberEntity.changeTariffBalance(hrsRetrieveDto.getTariffBalanceChange());
        return subscriberRepository.saveAndFlush(subscriberEntity);
    }
    //изменения баланса после помесячной тарификации
    public SubscriberEntity changeBalanceMonthTariffication(SubscriberEntity subscriberEntity, HrsRetrieveDto hrsRetrieveDto) {
        subscriberEntity.changeBalance(hrsRetrieveDto.getBalanceChange());
        subscriberEntity.setTariffBalance(hrsRetrieveDto.getTariffBalanceChange());
        return subscriberRepository.saveAndFlush(subscriberEntity);
    }

    //Обработка помесячной тарификации
    public SubscriberEntity monthTariffication(SubscriberEntity subscriber, LocalDateTime startCall) {
        LocalDateTime newDate = checkMonthTarifficationDate(startCall, subscriber);

        subscriber.setLastMonthTarifficationDate(newDate);
        HrsRetrieveDto hrsRetrieveDto = hrsRest.getMonthTariffFeeAndMinutes(subscriber.getTariffId());
        subscriber = changeBalanceMonthTariffication(subscriber, hrsRetrieveDto);
        subscriber.setTariffBalance(hrsRetrieveDto.getTariffBalanceChange());
        if (hrsRetrieveDto.getBalanceChange() != 0) {
            balanceChangesService.saveChangeEntity(hrsRetrieveDto.getBalanceChange(), subscriber, newDate);
        }
        log.info("Month tariffication passed successfully");
        log.error("===========================");

        return subscriber;
    }


    //Проверяем пора ли тарифицировать и устанавливаем новую дату
    private LocalDateTime checkMonthTarifficationDate(LocalDateTime startCall, SubscriberEntity subscriber) {
        LocalDateTime lastTariffication = subscriber.getLastMonthTarifficationDate();
        if (lastTariffication == null) {
            LocalDateTime registrationDate = subscriber.getRegistrationDate();
            long daysSinceRegistration = ChronoUnit.DAYS.between(registrationDate, startCall);
            long fullPeriods = daysSinceRegistration / 30; // количество полных 30-дневных периодов
            return registrationDate.plusDays(30 * fullPeriods);
        } else if (startCall.isBefore(lastTariffication.plusDays(30))) {
            throw new TooEarlyForTarifficationException(
                    "It's too early for tariffication. Next tariffication available after: "
                            + lastTariffication.plusDays(30)
            );
        }


        long daysPassed = ChronoUnit.DAYS.between(lastTariffication, startCall);
        long fullPeriods = daysPassed / 30;

        return lastTariffication.plusDays(30 * fullPeriods);

    }

    //сохраняем новичка
    public SubscriberEntity saveNewSub(SubscriberCrmDto subsCrm) {
        Optional<SubscriberEntity> subscriberOpt = subscriberRepository.findByMsisdn(subsCrm.getMsisdn());
        if (subscriberOpt.isPresent()) {
            throw new EntityAlreadyExsistsException("Subs with that msisdn already exists");
        }
        subscriberOpt = subscriberRepository.findByPassportData(subsCrm.getPassport());
        if (subscriberOpt.isPresent()) {
            throw new EntityAlreadyExsistsException("Subs with that passport already exists");
        }
        HrsTariffInfo hrsTariffInfo = hrsRest.getTariffById(subsCrm.getTariff());
        SubscriberEntity subscriber = SubscriberEntity.builder()
                .name(subsCrm.getFullName())
                .balance(subsCrm.getBalance())
                .isActive(true)
                .passportData(subsCrm.getPassport())
                .msisdn(subsCrm.getMsisdn())
                .lastMonthTarifficationDate(null)
                .registrationDate(LocalDateTime.now())
                .tariffId(subsCrm.getTariff())
                .tariffBalance(hrsTariffInfo.getTariffParametr().getMonthlyMinuteCapacity())
                .build();

        return subscriberRepository.saveAndFlush(subscriber);


    }

    //получаем старичка
    public SubscriberEntity getSubscriber(String msisdn) {
        SubscriberEntity subscriber = subscriberRepository.findByMsisdn(msisdn).orElseThrow(() -> new EntityNotFoundException("Subs with that msisdn not Found"));
        return subscriber;
    }


    public SubscriberEntity updateSub(SubscriberCrmDto subsCrm) {
        SubscriberEntity subscriber = subscriberRepository.findByMsisdn(subsCrm.getMsisdn()).orElseThrow(() -> new EntityNotFoundException("Subs with that msisdn is not exists"));
        HrsTariffInfo hrsTariffInfo = hrsRest.getTariffById(subsCrm.getTariff());
        SubscriberEntity updatedSubscriber = SubscriberEntity.builder()
                .id(subscriber.getId())
                .name(subsCrm.getFullName())
                .balance(subsCrm.getBalance())
                .isActive(true)
                .passportData(subsCrm.getPassport())
                .msisdn(subsCrm.getMsisdn())
                .lastMonthTarifficationDate(null)
                .registrationDate(LocalDateTime.now())
                .tariffId(subsCrm.getTariff())
                .tariffBalance(hrsTariffInfo.getTariffParametr().getMonthlyMinuteCapacity())
                .build();

        return subscriberRepository.saveAndFlush(updatedSubscriber);
    }


    public DeleteStatusDto deleteSub(String msisdn) {
        SubscriberEntity subscriber = subscriberRepository.findByMsisdn(msisdn).orElseThrow(() -> new EntityNotFoundException("This sub is not exist"));
        subscriberRepository.delete(subscriber);
        return new DeleteStatusDto(msisdn, "deleted");
    }

    public SubscriberEntity changeTariff(String msisdn, Long newTariffId) {
        SubscriberEntity subscriber = subscriberRepository.findByMsisdn(msisdn).orElseThrow(() -> new EntityNotFoundException("Sub with that msisdn is not exists"));

        if (subscriber.getTariffId().equals(newTariffId)) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "у него уже этот тариф");
        }

        if (!hrsRest.getTariffById(newTariffId).isActive()) {
            throw new ClientException(HttpStatus.BAD_REQUEST, "тариф не активен");
        }
        subscriber.setTariffId(newTariffId);
        return subscriberRepository.saveAndFlush(subscriber);
    }

    public SubscriberEntity changeSubBalance(String msisdn, ChangeBalanceDto changeBalanceDto) {
        SubscriberEntity subscriber = subscriberRepository.findByMsisdn(msisdn).orElseThrow(() -> new EntityNotFoundException("Subs with that msisdn is not exists"));
        balanceChangesService.saveChangeEntity(changeBalanceDto.getAmount(), subscriber, LocalDateTime.now());
        subscriber.changeBalance(changeBalanceDto.getAmount());
        return subscriberRepository.saveAndFlush(subscriber);
    }
}

