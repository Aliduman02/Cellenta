package com.i2i.intern.cellenta.chf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VoltDbBalanceResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("results")
    private List<BalanceResult> results;
    
    // Constructors
    public VoltDbBalanceResponse() {}
    
    public VoltDbBalanceResponse(String status, List<BalanceResult> results) {
        this.status = status;
        this.results = results;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<BalanceResult> getResults() { return results; }
    public void setResults(List<BalanceResult> results) { this.results = results; }
    
    @Override
    public String toString() {
        return "VoltDbBalanceResponse{" +
                "status='" + status + '\'' +
                ", results=" + results +
                '}';
    }
    
    // Inner class for results
    public static class BalanceResult {
        
        @JsonProperty("MSISDN")
        private Long msisdn;
        
        @JsonProperty("REMAINING_MINUTES")
        private Integer remainingMinutes;
        
        @JsonProperty("REMAINING_SMS")
        private Integer remainingSms;
        
        @JsonProperty("REMAINING_DATA")
        private Integer remainingData;
        
        @JsonProperty("START_DATE")
        private DateObject startDate;  // ✅ Object olarak değiştirildi
        
        @JsonProperty("END_DATE")
        private DateObject endDate;    // ✅ Object olarak değiştirildi
        
        @JsonProperty("PACKAGE_NAME")
        private String packageName;
        
        @JsonProperty("PRICE")
        private Integer price;
        
        @JsonProperty("AMOUNT_MINUTES")
        private Integer amountMinutes;
        
        @JsonProperty("AMOUNT_SMS")
        private Integer amountSms;
        
        @JsonProperty("AMOUNT_DATA")
        private Integer amountData;
        
        @JsonProperty("PERIOD")
        private Integer period;
        
        // Constructors
        public BalanceResult() {}
// Getters and Setters
        public Long getMsisdn() { return msisdn; }
        public void setMsisdn(Long msisdn) { this.msisdn = msisdn; }
        
        public Integer getRemainingMinutes() { return remainingMinutes; }
        public void setRemainingMinutes(Integer remainingMinutes) { this.remainingMinutes = remainingMinutes; }
        
        public Integer getRemainingSms() { return remainingSms; }
        public void setRemainingSms(Integer remainingSms) { this.remainingSms = remainingSms; }
        
        public Integer getRemainingData() { return remainingData; }
        public void setRemainingData(Integer remainingData) { this.remainingData = remainingData; }
        
        public DateObject getStartDate() { return startDate; }
        public void setStartDate(DateObject startDate) { this.startDate = startDate; }
        
        public DateObject getEndDate() { return endDate; }
        public void setEndDate(DateObject endDate) { this.endDate = endDate; }
        
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public Integer getPrice() { return price; }
        public void setPrice(Integer price) { this.price = price; }
        
        public Integer getAmountMinutes() { return amountMinutes; }
        public void setAmountMinutes(Integer amountMinutes) { this.amountMinutes = amountMinutes; }
        
        public Integer getAmountSms() { return amountSms; }
        public void setAmountSms(Integer amountSms) { this.amountSms = amountSms; }
        
        public Integer getAmountData() { return amountData; }
        public void setAmountData(Integer amountData) { this.amountData = amountData; }
        
        public Integer getPeriod() { return period; }
        public void setPeriod(Integer period) { this.period = period; }
        
        // Helper methods to get date strings
        public String getStartDateString() {
            return startDate != null ? startDate.toDateString() : null;
        }
        
        public String getEndDateString() {
            return endDate != null ? endDate.toDateString() : null;
        }
        
        @Override
        public String toString() {
            return "BalanceResult{" +
                    "msisdn=" + msisdn +
                    ", remainingMinutes=" + remainingMinutes +
                    ", remainingSms=" + remainingSms +
                    ", remainingData=" + remainingData +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    ", packageName='" + packageName + '\'' +
                    ", price=" + price +
                    ", amountMinutes=" + amountMinutes +
                    ", amountSms=" + amountSms +
                    ", amountData=" + amountData +
                    ", period=" + period +
                    '}';
        }
    }

// Inner class for date objects
    public static class DateObject {
        private static final ZoneId ISTANBUL_ZONE = ZoneId.of("Europe/Istanbul");
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        @JsonProperty("usec")
        private Long usec;
        
        @JsonProperty("time")
        private Long time;
        
        // Constructors
        public DateObject() {}
        
        public DateObject(Long usec, Long time) {
            this.usec = usec;
            this.time = time;
        }
        
        // Getters and Setters
        public Long getUsec() { return usec; }
        public void setUsec(Long usec) { this.usec = usec; }
        
        public Long getTime() { return time; }
        public void setTime(Long time) { this.time = time; }
        
	// Convert to readable date string - İstanbul zaman dilimi ile
	public String toDateString() {
	    if (time == null) return null;
	    try {
       	// Convert microseconds to milliseconds
	        long milliseconds = time / 1000;
        
        // İstanbul zaman diliminde formatla - SADECE BU METOD DEĞİŞTİ
        ZonedDateTime istanbulTime = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(milliseconds),
            ZoneId.of("Europe/Istanbul")
        	);
        
        	return istanbulTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    	} catch (Exception e) {
        	// Hata durumunda null döndür
        	return null;
    		}
	}        
        @Override
        public String toString() {
            return "DateObject{" +
                    "usec=" + usec +
                    ", time=" + time +
                    '}';
        }
    }
}

