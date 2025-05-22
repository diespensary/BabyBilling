package com.example.cdr.mapper;

import com.example.cdr.dto.CdrDto;
import com.example.cdr.entity.CallEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface MyMapper {
    List<CdrDto> callEntityListToDto(List<CallEntity> cdrEntities, @Context String number);
}
