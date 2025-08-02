import fs from "fs/promises";
import path from "path";

export async function smsLogger({ to, message, tip, tur }) {
  const logPath = path.join("logs", "mock-sms-log.json");
  const logEntry = {
    msisdn: to,
    message: message,
    success: true,
    usage_type: tip,
    notification_message: tur,
    timestamp: new Date().toISOString(),
  };
  await fs.mkdir("logs", { recursive: true });
  let existing = [];
  try {
    const data = await fs.readFile(logPath, "utf-8");
    if (data.trim() !== "") {
      const parsedData = JSON.parse(data);
      existing = Array.isArray(parsedData) ? parsedData : [];
    }
  } catch (err) {
    if (err.code !== "ENOENT") throw err;
  }
  existing.push(logEntry);
  await fs.writeFile(logPath, JSON.stringify(existing, null, 2));
  return {
    logStatus: 200,
  };
}
