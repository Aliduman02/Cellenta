package com.cellenta.cgf.service;

import com.cellenta.cgf.dto.UsageRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class CGFConsumer {

    private static final Logger log = LoggerFactory.getLogger(CGFConsumer.class);

    @Autowired
    private UsageRecordService usageRecordService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "chf-to-cgf", groupId = "cgf-consumer-group")
    public void consume(String message) {
        try {
            log.info("📥 Kafka'dan gelen mesaj: {}", message);
            UsageRecord record = objectMapper.readValue(message, UsageRecord.class);

            int status = usageRecordService.insertUsage(
                    record.getGiver_msisdn(),
                    record.getUsage_type(),
                    Timestamp.valueOf(record.getUsage_date()), // string to timestamp
                    record.getUsage_duration(),
                    record.getReceiver_msisdn()
            );

            log.info("➡️ Oracle prosedürü çağrıldı, status: {}", status);

        } catch (Exception e) {
            log.error("❌ Kafka mesajı işlenemedi: {}", e.getMessage(), e);
        }
    }

}
