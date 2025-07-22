package com.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.kafka.serializer.BalanceMessageSerializer;
import com.kafka.serializer.UsageRecordMessageSerializer;
import com.kafka.serializer.NotificationMessageSerializer;
import com.kafka.message.Message;

import java.util.Properties;

public class MessageProducer<T extends Message> implements AutoCloseable {

    private Producer<String, T> producer;

    private Producer<String, T> createProducer(String ClassName) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigLoader.getProperty("kafka.url"));
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ClassName);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);

        return new KafkaProducer<>(properties);
    }

    public void createBalanceMessageProducer() {
        producer = createProducer(BalanceMessageSerializer.class.getName());
    }

    public void createUsageRecordMessageProducer() {
        producer = createProducer(UsageRecordMessageSerializer.class.getName());
    }

    public void createNotificationMessageProducer() {
        producer = createProducer(NotificationMessageSerializer.class.getName());
    }

    public void send(T message, String topicName) {
        if (producer != null) {
            producer.send(new ProducerRecord<>(topicName, "operation", message), (metadata, exception) -> {
                if (exception != null) {
                    // Log or handle the exception
                    System.err.println("Error sending message: " + exception.getMessage());
                } else {
                    // Log success metadata
                    System.out.println("Message sent successfully!");
                    System.out.println("Topic: " + metadata.topic() +
                            ", Partition: " + metadata.partition() +
                            ", Offset: " + metadata.offset());
                }
            });
        }
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.close();
            System.out.println("Kafka producer closed.");
        }
    }

    // Backward compatibility static method
    public static <T> void produce(String topic, String key, T message, Class<?> serializerClass) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigLoader.get("kafka.bootstrap.servers", "localhost:9092"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);

        try (KafkaProducer<String, T> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, T> record = new ProducerRecord<>(topic, key, message);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Mesaj gönderim hatası:");
                    exception.printStackTrace();
                } else {
                    System.out.printf("Mesaj gönderildi: topic=%s, partition=%d, offset=%d, key=%s\n",
                            metadata.topic(), metadata.partition(), metadata.offset(), key);
                }
            });
            producer.flush();
        }
    }
} 