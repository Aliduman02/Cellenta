package com.cellenta.cgf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.MessageConsumer;
import com.kafka.message.Message;
import com.kafka.deserializer.UsageRecordMessageDeserializer;
import com.kafka.KafkaTopicConstants;
import com.cellenta.cgf.dto.UsageRecord;
import com.kafka.message.UsageRecordMessage;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

@Service
public class KafkaJarConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UsageRecordService usageRecordService;

    private MessageConsumer<UsageRecordMessage> consumer;

    @PostConstruct
    public void startKafkaConsumer() {
        consumer = new MessageConsumer<>();
        consumer = createUsageConsumer();

        new Thread(() -> {
            while (true) {
                try {
                    ConsumerRecords<String, UsageRecordMessage> records = consumer.poll();
                    if (records != null) {
                        records.forEach(record -> {
                            UsageRecordMessage usage = record.value();
                            try {
                                System.out.println("Gelen mesaj: " + objectMapper.writeValueAsString(usage));
                                int status = usageRecordService.insertUsage(
                                        usage.getGiverMsisdn(),
                                        usage.getUsageType(),
                                        Timestamp.valueOf(usage.getUsageDate()),
                                        usage.getUsageDuration(),
                                        usage.getReceiverMsisdn()
                                );
                                System.out.println("Oracle prosedürü çağrıldı, status: " + status);
                            } catch (Exception e) {
                                System.err.println("DB işlemi hatası: " + e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Kafka polling hatası: " + e.getMessage());
                }
            }
        }).start();
    }

    private MessageConsumer<UsageRecordMessage> createUsageConsumer() {
        MessageConsumer<UsageRecordMessage> cons = new MessageConsumer<>();
        cons.createUsageRecordMessageConsumer(); // Bu zaten UsageRecordMessageDeserializer ile bağlanıyor
        return cons;
    }
}
