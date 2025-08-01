package com.resetSercice.controller;

import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

@RestController
// @RequestMapping("/password-reset-service")  // bu satırı kaldırdım
public class StandardHealthController {

    private static final Logger logger = LoggerFactory.getLogger(StandardHealthController.class);
    private static final String SERVICE_NAME = "Password Reset Service";
    private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalDateTime startTime = LocalDateTime.now(ISTANBUL_ZONE);

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("🏥 Health check requested for service: {}", SERVICE_NAME);

        Map<String, Object> healthData = new HashMap<>();

        try {
            healthData.put("status", 200);
            healthData.put("service", SERVICE_NAME);
            healthData.put("jar_status", "RUNNING");
            healthData.put("timestamp", ZonedDateTime.now(ISTANBUL_ZONE).format(formatter));
            healthData.put("uptime", formatUptime());
            addSystemMetrics(healthData);

            logger.info("✅ Health check successful - CPU: {}%, Memory: {}%, Disk: {}%, Uptime: {}",
                    healthData.get("cpu_percent"), healthData.get("memory_percent"),
                    healthData.get("disk_percent"), healthData.get("uptime"));

            return ResponseEntity.ok(healthData);

        } catch (Exception e) {
            logger.error("❌ Health check failed for service: {}, Error: {}", SERVICE_NAME, e.getMessage());

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("status", 503);
            errorData.put("service", SERVICE_NAME);
            errorData.put("jar_status", "ERROR");
            errorData.put("error", e.getMessage());
            errorData.put("timestamp", ZonedDateTime.now(ISTANBUL_ZONE).format(formatter));

            return ResponseEntity.status(503).body(errorData);
        }
    }

    private void addSystemMetrics(Map<String, Object> healthData) {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            double cpuPercent = osBean.getProcessCpuLoad() * 100;
            if (cpuPercent < 0) cpuPercent = 0;
            healthData.put("cpu_percent", Math.round(cpuPercent * 10.0) / 10.0);

            long totalMemory = osBean.getTotalPhysicalMemorySize();
            long freeMemory = osBean.getFreePhysicalMemorySize();
            long usedMemory = totalMemory - freeMemory;
            double memoryPercent = (double) usedMemory / totalMemory * 100;
            healthData.put("memory_percent", Math.round(memoryPercent * 10.0) / 10.0);

            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskPercent = (double) usedSpace / totalSpace * 100;
            healthData.put("disk_percent", Math.round(diskPercent * 10.0) / 10.0);

        } catch (Exception e) {
            healthData.put("cpu_percent", -1);
            healthData.put("memory_percent", -1);
            healthData.put("disk_percent", -1);
        }
    }

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
