import React from "react";
import AnimatedCircularProgressBar from "./AnimatedCircularProgressBar";

export default function TariffInfo({ usageData }) {
  // API'den gelen veriyi direkt kullan
  const data = usageData || {};

  // MB'den GB'ye dönüşüm fonksiyonu
  const mbToGb = (mb) => (mb ? parseFloat((mb / 1000).toFixed(2)) : 0);

  // API'den gelen veriyi component formatına çevir
  const formattedData = {
    minutes: { 
      used: Math.max(0, (data.totalMinutes || 0) - (data.remainingMinutes || 0)), 
      total: data.totalMinutes || 0, 
      remaining: data.remainingMinutes || 0 
    },
    data: { 
      used: Math.max(0, (data.totalData || 0) - (data.remainingData || 0)), 
      total: data.totalData || 0, 
      remaining: data.remainingData || 0 
    },
    sms: { 
      used: Math.max(0, (data.totalSms || 0) - (data.remainingSms || 0)), 
      total: data.totalSms || 0, 
      remaining: data.remainingSms || 0 
    }
  };

  return (
    <section
      className="tariff-info"
      style={{
        position: "relative",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
      }}
    >
      <div className="tariff-info-title">Tarife Bilgileri</div>
      <div className="circle-group">
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
          <AnimatedCircularProgressBar
            value={formattedData.minutes.remaining}
            max={formattedData.minutes.total}
            unit="DK"
            gaugePrimaryColor="#14b8a6"
          />
          <div style={{ marginTop: 4, fontSize: "1.1rem", fontWeight: 500, color: "#444" }}>
            {formattedData.minutes.total} DK'dan {formattedData.minutes.remaining} DK kaldı
          </div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
          <AnimatedCircularProgressBar
            value={mbToGb(formattedData.data.remaining)}
            max={mbToGb(formattedData.data.total)}
            unit="GB"
            gaugePrimaryColor="#0ea5e9"
          />
          <div style={{ marginTop: 4, fontSize: "1.1rem", fontWeight: 500, color: "#444" }}>
            {mbToGb(formattedData.data.total).toFixed(2)} GB'dan {mbToGb(formattedData.data.remaining).toFixed(2)} GB kaldı
          </div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
          <AnimatedCircularProgressBar
            value={formattedData.sms.remaining}
            max={formattedData.sms.total}
            unit="SMS"
            gaugePrimaryColor="#a21caf"
          />
          <div style={{ marginTop: 4, fontSize: "1.1rem", fontWeight: 500, color: "#444" }}>
            {formattedData.sms.total} SMS'den {formattedData.sms.remaining} SMS kaldı
          </div>
        </div>
      </div>
    </section>
  );
}
