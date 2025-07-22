package com.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.kafka.deserializer.BalanceMessageDeserializer;
import com.kafka.deserializer.NotificationMessageDeserializer;
import com.kafka.deserializer.UsageRecordMessageDeserializer;
import com.kafka.deserializer.DebugBalanceDeserializer;
import com.kafka.KafkaTopicConstants;
import com.kafka.message.Message;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MessageConsumer<T extends Message> {

    public KafkaConsumer<String, T> consumer;

    private KafkaConsumer<String, T> createConsumer(String deserializerClassName, String topicName, String groupId) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigLoader.getProperty("kafka.url"));
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClassName);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put("auto.offset.reset", "latest");

        KafkaConsumer<String, T> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topicName));

        return consumer;
    }

    public void createBalanceMessageConsumer() {
        consumer = createConsumer(BalanceMessageDeserializer.class.getName(), KafkaTopicConstants.ABMF_USAGE_TOPIC,
                "BalanceConsumerGroup-" + System.currentTimeMillis());
    }

    public void createUsageRecordMessageConsumer() {
        consumer = createConsumer(UsageRecordMessageDeserializer.class.getName(), KafkaTopicConstants.CGF_USAGE_TOPIC,
                "UsageRecordConsumerGroup");
    }

    public void createNotificationMessageConsumer() {
        consumer = createConsumer(NotificationMessageDeserializer.class.getName(), KafkaTopicConstants.USAGE_RECORD_TOPIC,
                "NotificationConsumerGroup");
    }

    public ConsumerRecords<String, T> poll() {
        if (consumer != null) {
            System.out.println("Subscribed topics: " + consumer.subscription());
            try {
                ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(1000));
                System.out.println("Polled record count: " + records.count());
                return records;
            } catch (Exception e) {
                if (e.getMessage().contains("Error deserializing")) {
                    System.out.println("Deserialization error detected, seeking to end...");
                    seekToEnd();
                    return null;
                }
                throw e;
            }
        } else {
            return null;
        }
    }

    public void seekToEnd() {
        if (consumer != null) {
            consumer.seekToEnd(consumer.assignment());
            System.out.println("Consumer seeked to end");
        }
    }

    public ConsumerRecords<String, T> poll(Duration duration) {
        if (consumer != null) {
            System.out.println("Subscribed topics: " + consumer.subscription());
            ConsumerRecords<String, T> records = consumer.poll(duration);
            System.out.println("Polled record count: " + records.count());
            return records;
        } else {
            return null;
        }
    }

    // Backward compatibility static method
    public static <T> void consume(String topic, Class<?> deserializerClass, Class<T> messageType) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigLoader.get("kafka.bootstrap.servers", "localhost:9092"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, topic + "-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, T> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            System.out.println("Consumer dinliyor: " + topic);
            while (true) {
                ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(1000));
                for (var record : records) {
                    T msg = record.value();
                    System.out.printf("Mesaj alındı: %s\n", msg);
                }
            }
        }
    }
} 