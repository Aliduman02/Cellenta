package com.cellenta.cgf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KafkaLogProcessorService {

    private static final String KAFKA_LOG_PATH = "/home/asmeydan0816/kafkaJar/kafka.log"; // yolunu özelleştir
    private long lastReadLineCount = 0;

    @Autowired
    private UsageRecordService usageRecordService;

    private final Pattern pattern = Pattern.compile(
            "UsageRecordMessage\\{giverMsisdn='(.*?)', receiverMsisdn='(.*?)', usageDate='(.*?)', usageType='(.*?)', usageDuration=(\\d+)\\}");

    @PostConstruct
    public void init() {
        System.out.println("Kafka log processor başlatıldı.");
    }

    @Scheduled(fixedDelay = 5000) // her 5 saniyede bir tarama yap
    public void processKafkaLog() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(KAFKA_LOG_PATH));
            if (lines.size() > lastReadLineCount) {
                List<String> newLines = lines.subList((int) lastReadLineCount, lines.size());
                for (String line : newLines) {
                    if (line.contains("UsageRecordMessage{")) {
                        System.out.println("line: " + line);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String giverMsisdn = matcher.group(1);
                            String receiverMsisdn = matcher.group(2);
                            String usageDate = matcher.group(3);
                            String usageType = matcher.group(4);
                            int duration = Integer.parseInt(matcher.group(5));

                            if ("null".equals(receiverMsisdn)) {
                                receiverMsisdn = null;
                            }

                            Timestamp timestamp = Timestamp.valueOf(usageDate);

                            int status = usageRecordService.insertUsage(
                                    giverMsisdn, usageType, timestamp, duration, receiverMsisdn
                            );
                            System.out.println("Veri işlendi, status: " + status);
                        }
                    }
                }
                lastReadLineCount = lines.size();
            }
        } catch (IOException e) {
            System.err.println("Log dosyası okunamadı: " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("Veri işleme hatası: " + ex.getMessage());
        }
    }
}
