package com.cellenta.cgf.service;

import com.cellenta.cgf.repository.UsageRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class UsageRecordService {

    @Autowired
    private UsageRecordRepository repository;

    public int insertUsage(
            String giverMsisdn,
            String usageType,
            Timestamp usageDate,
            int duration,
            String receiverMsisdn
    ) {
        // Logic
        return repository.callInsertUsageProcedure(
                giverMsisdn, usageType, usageDate, duration, receiverMsisdn
        );
    }
}
