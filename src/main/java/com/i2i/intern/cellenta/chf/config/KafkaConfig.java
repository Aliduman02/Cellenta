package com.i2i.intern.cellenta.chf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chf.kafka")
public class KafkaConfig {
    
    private Topics topics;
    
    public Topics getTopics() { return topics; }
    public void setTopics(Topics topics) { this.topics = topics; }
    
    public static class Topics {
        private String abmf;
        private String cgf;
        private String notification;
        
        // Getters and Setters
        public String getAbmf() { return abmf; }
        public void setAbmf(String abmf) { this.abmf = abmf; }
        
        public String getCgf() { return cgf; }
        public void setCgf(String cgf) { this.cgf = cgf; }
        
        public String getNotification() { return notification; }
        public void setNotification(String notification) { this.notification = notification; }
    }
}
