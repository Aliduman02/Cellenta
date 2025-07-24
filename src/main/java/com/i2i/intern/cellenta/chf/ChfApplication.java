package com.i2i.intern.cellenta.chf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;  // javax değil jakarta!
import java.util.TimeZone;

@SpringBootApplication
public class ChfApplication {
    
    /**
     * JVM seviyesinde Istanbul zaman dilimini ayarla
     * Bu tüm uygulama için default timezone'u İstanbul yapar
     */
    @PostConstruct
    public void init() {
        // Uygulama başlarken İstanbul zaman dilimini varsayılan olarak ayarla
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));
        System.out.println("Application timezone set to: " + TimeZone.getDefault().getDisplayName());
        System.out.println("Current time in Istanbul: " + new java.util.Date().toString());
    }
    
    public static void main(String[] args) {
        SpringApplication.run(ChfApplication.class, args);
    }
}
