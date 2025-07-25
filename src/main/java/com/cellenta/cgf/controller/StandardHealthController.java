package com.cellenta.cgf.controller;

import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 🏥 STANDARD HEALTH CONTROLLER - CHF
 * 📋 Sistem durumu ve metrikleri için standart endpoint
 */
@RestController
@RequestMapping("/CGF")
public class StandardHealthController {

    private static final Logger logger = LoggerFactory.getLogger(StandardHealthController.class);

    private static final String SERVICE_NAME = "CGF";

    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Service başlangıç zamanı
    private static final LocalDateTime startTime = LocalDateTime.now(ISTANBUL_ZONE);

    /**
     * 🏥 STANDARD HEALTH ENDPOINT
     * GET /health
     *
     * Response Format:
     * - status: 200 (OK), 503 (Service Unavailable)
     * - service: CHF
     * - system metrics: CPU, Memory, Disk kullanımı
     * - jar_status: RUNNING/ERROR
     * - uptime: Ne kadar süredir çalışıyor
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("🏥 Health check requested for service: {}", SERVICE_NAME);

        Map<String, Object> healthData = new HashMap<>();

        try {
            // 🚀 Basic Info
            healthData.put("status", 200); // HTTP Status Code format
            healthData.put("service", SERVICE_NAME);
            healthData.put("jar_status", "RUNNING"); // Endpoint'e cevap veriyorsa jar çalışıyor

            // ⏰ Timestamp (İstanbul timezone)
            healthData.put("timestamp", ZonedDateTime.now(ISTANBUL_ZONE).format(formatter));

            // ⏱️ Uptime
            healthData.put("uptime", formatUptime());

            // 💾 System Metrics
            addSystemMetrics(healthData);

            logger.info("✅ Health check successful - CPU: {}%, Memory: {}%, Disk: {}%, Uptime: {}",
                    healthData.get("cpu_percent"), healthData.get("memory_percent"),
                    healthData.get("disk_percent"), healthData.get("uptime"));

            return ResponseEntity.ok(healthData);

        } catch (Exception e) {
            // 🚨 Error durumunda
            logger.error("❌ Health check failed for service: {}, Error: {}", SERVICE_NAME, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("status", 503); // Service Unavailable
            errorData.put("service", SERVICE_NAME);
            errorData.put("jar_status", "ERROR");
            errorData.put("error", e.getMessage());
            errorData.put("timestamp", ZonedDateTime.now(ISTANBUL_ZONE).format(formatter));

            return ResponseEntity.status(503).body(errorData);
        }
    }

    /**
     * 💾 System metrics ekle
     */
    private void addSystemMetrics(Map<String, Object> healthData) {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            // 🔥 CPU Usage
            double cpuPercent = osBean.getProcessCpuLoad() * 100;
            if (cpuPercent < 0) cpuPercent = 0; // İlk ölçümde -1 dönebilir
            healthData.put("cpu_percent", Math.round(cpuPercent * 10.0) / 10.0);

            // 💾 Memory Usage
            long totalMemory = osBean.getTotalPhysicalMemorySize();
            long freeMemory = osBean.getFreePhysicalMemorySize();
            long usedMemory = totalMemory - freeMemory;
            double memoryPercent = (double) usedMemory / totalMemory * 100;
            healthData.put("memory_percent", Math.round(memoryPercent * 10.0) / 10.0);

            // 💿 Disk Usage
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskPercent = (double) usedSpace / totalSpace * 100;
            healthData.put("disk_percent", Math.round(diskPercent * 10.0) / 10.0);

        } catch (Exception e) {
            // Metrics alınamazsa default değerler
            healthData.put("cpu_percent", -1);
            healthData.put("memory_percent", -1);
            healthData.put("disk_percent", -1);
        }
    }

    /**
     * ⏱️ Uptime'ı human readable formata çevir
     */
    private String formatUptime() {
        Duration uptime = Duration.between(startTime, LocalDateTime.now(ISTANBUL_ZONE));

        long days = uptime.toDays();
        long hours = uptime.toHours() % 24;
        long minutes = uptime.toMinutes() % 60;
        long seconds = uptime.getSeconds() % 60;

        StringBuilder uptimeStr = new StringBuilder();

        if (days > 0) uptimeStr.append(days).append("d ");
        if (hours > 0) uptimeStr.append(hours).append("h ");
        if (minutes > 0) uptimeStr.append(minutes).append("m ");
        uptimeStr.append(seconds).append("s");

        return uptimeStr.toString();
    }
}