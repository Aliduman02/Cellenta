package com.abmf.kafkaconsumer.service;

import com.abmf.kafkaconsumer.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class LogFileTailService {

    private static final String LOG_FILE_PATH = "logs/kafka.log";

    @Autowired
    private BalanceService balanceService;

    private final Map<String, CachedBalance> bufferMap = new ConcurrentHashMap<>();

    // Veriyi oku
    @PostConstruct
    public void startTailingLogFile() {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                RandomAccessFile file = new RandomAccessFile(Paths.get(LOG_FILE_PATH).toFile(), "r");
                file.seek(file.length());

                System.out.println("📄 Log takibi başlatıldı: " + LOG_FILE_PATH);

                while (true) {
                    String line = file.readLine();
                    if (line != null) {
                        handleLogLine(line);
                    } else {
                        Thread.sleep(10);
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Log dosyası okunurken hata: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // 5 dakikada bir buffer'ı Oracle'a yaz
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("🕔 5 dakika doldu. Oracle'a batch yazılıyor...");

            bufferMap.forEach((msisdn, balance) -> {
                balanceService.updateBalance(msisdn, balance.minutes, balance.sms, balance.data);
                System.out.println("✅ Oracle'a yazıldı (Batch): " + msisdn + " | Dakika: " + balance.minutes + " | SMS: " + balance.sms + " | Data: " + balance.data);
            });

            bufferMap.clear();
            System.out.println("🧹 Buffer temizlendi.");
        }, 5, 5, TimeUnit.MINUTES);
    }

    // Log satırını işle
    private void handleLogLine(String line) {
        if (line.contains("Value: BalanceMessage{")) {
            try {
                String prefix = "Value: BalanceMessage{";
                int start = line.indexOf(prefix);
                int end = line.lastIndexOf("}");

                if (start != -1 && end != -1) {
                    String balancePart = line.substring(start + prefix.length(), end);
                    String[] fields = balancePart.split(",");

                    String msisdn = null;
                    int minutes = 0, sms = 0, data = 0;

                    for (String field : fields) {
                        String[] keyValue = field.trim().split("=");
                        if (keyValue.length == 2) {
                            switch (keyValue[0].trim()) {
                                case "msisdn":
                                    msisdn = keyValue[1].replace("'", "");
                                    break;
                                case "new_minutes":
                                    minutes = Integer.parseInt(keyValue[1]);
                                    break;
                                case "new_sms":
                                    sms = Integer.parseInt(keyValue[1]);
                                    break;
                                case "new_data":
                                    data = Integer.parseInt(keyValue[1]);
                                    break;
                            }
                        }
                    }

                    if (msisdn != null) {
                        bufferMap.put(msisdn, new CachedBalance(minutes, sms, data));
                        System.out.println("🧠 Buffer'a güncellendi: " + msisdn);
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Parse hatası: " + e.getMessage());
            }
        } else {
            System.out.println("⏭️ Atlanan satır: " + line);
        }
    }

    // Buffer içeriği için model class
    static class CachedBalance {
        public int minutes;
        public int sms;
        public int data;

        public CachedBalance(int minutes, int sms, int data) {
            this.minutes = minutes;
            this.sms = sms;
            this.data = data;
        }
    }
}
