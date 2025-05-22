package com.example.cdr.services;

import com.example.demo.dto.CdrDto;
import com.example.demo.entity.CallEntity;
import com.example.demo.mapper.MyMapper;
import com.example.demo.rabbit.RmqProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CdrService {
    @Autowired
    MyMapper mapper;
    @Autowired
    RmqProducer rmqProducer;

    public void sendCdrReport(String msisdn, List<CallEntity> callEntities) {
        List<CdrDto> cdrDtos = mapper.callEntityListToDto(callEntities, msisdn);
        rmqProducer.sendJsonMassage(cdrDtos);
    }

}
