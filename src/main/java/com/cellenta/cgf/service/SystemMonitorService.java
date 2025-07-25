package com.cellenta.cgf.service;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SystemMonitorService {

    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = systemInfo.getHardware();

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        CentralProcessor processor = hal.getProcessor();
        GlobalMemory memory = hal.getMemory();

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Uyku kesildi: " + e.getMessage());
        }

        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();

        stats.put("cpuLoadPercentage", String.format("%.2f", cpuLoad));
        stats.put("totalMemoryMB", totalMemory / (1024 * 1024));
        stats.put("availableMemoryMB", availableMemory / (1024 * 1024));
        stats.put("usedMemoryMB", (totalMemory - availableMemory) / (1024 * 1024));

        return stats;
    }
}
