import { smsLogger } from "./smsLogger.js";
export async function smsSender({ to, parsed }) {
  const total = (type, parsed) => {
    if (!type || !parsed) return "";

    const lower = type.toLowerCase();
    const map = {
      data: "total_data",
      sms: "total_sms",
      minutes: "total_minutes",
    };
    const key = map[lower];
    let value = parsed?.[key];
    if (lower === "data" && value) {
      const gb = (Number(value) / 1000).toFixed(1);
      return gb;
    }
    return value || "";
  };
  const usagetype = (type) => {
    if (!type) return "";
    const lower = type.toLowerCase();
    if (lower === "data") return "GB";
    if (lower === "minutes") return "dakika";
    if (lower === "sms") return "SMS";
    console.log("type:", type);
    return type;
  };

  const tur = parsed.notification_message;
  const tip = parsed.usage_type;
  const message = `Sayın ${parsed.name}, ${new Date(
    parsed.timestamp
  ).toLocaleString("tr-TR", {
    hour: "2-digit",
    minute: "2-digit",
  })} itibariyle ${parsed.package_name} kapsamindaki ${total(
    parsed.usage_type,
    parsed
  )} ${usagetype(parsed.usage_type)} hakknizin %${
    parsed.percentage
  }'ini kullandiniz. Kalan kullanim haklarinizi Celi'den öğrenebilirsiniz. `;
  console.log("🟡 SMS Mesaj'ı gönderildi...");
  const logstatus = await smsLogger({
    to,
    message,
    tip,
    tur,
  });
  return {
    success: true,
    to,
    message: message,
    logstatus,
  };
}
