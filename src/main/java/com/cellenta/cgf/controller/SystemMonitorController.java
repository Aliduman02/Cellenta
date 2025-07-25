package com.cellenta.cgf.controller;

import com.cellenta.cgf.service.SystemMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SystemMonitorController {

    @Autowired
    private SystemMonitorService monitorService;

    @GetMapping("/system/status")
    public Map<String, Object> getSystemStatus() {
        return monitorService.getSystemStats();
    }
}
