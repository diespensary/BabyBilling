package brtApp.mapper;

import brtApp.dto.HrsCallDto;
import brtApp.entity.CallEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MyMapper {
    @Mapping(target = "minutes", source = ".", qualifiedByName = "calculateMinutesRoundedUp")
    @Mapping(target = "callType", source = "callType.id")
    @Mapping(target = "isRomashkaCall", source = "isRomashkaCall")
    @Mapping(target = "tariffId", source = "subscriber.tariffId")
    @Mapping(target = "tariffBalance", source = "subscriber.tariffBalance")
    @Mapping(target = "balance", source = "subscriber.balance")
    HrsCallDto callEntityToHrsCallDto(CallEntity callEntity);
}
