import { log } from "console";
import { Router } from "express";
import { promises as fs } from "fs";
import path from "path";

const router = Router();

router.get("/sms-logs", async (req, res) => {
  const logPath = path.join("logs", "mock-sms-log.json");

  try {
    const data = await fs.readFile(logPath, "utf-8");
    const logs = JSON.parse(data);
    res.status(200).json(logs);
  } catch (err) {
    console.error("❌ Log okuma hatası:", err.message);
    res.status(500).json({ error: "Loglar okunamadı." });
  }
});

router.get("/sms", async (req, res) => {
  const logPath = path.join("logs", "mock-sms-log.json");
  try {
    const data = await fs.readFile(logPath, "utf-8");

    const logs = JSON.parse(data);
    let html = `
      <!DOCTYPE html>
      <html><head><meta charset="UTF-8"><title>SMS Logları</title>
      <style>table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}</style>
      </head><body><div style="display:flex;align-items:center;justify-content:space-between; padding:0 20px;"><h2>Cellenta SMS Logları</h2></div><table>
      <tr><th>Telefon</th><th>Mesaj</th><th>Kullanım Tipi</th><th>Tür</th><th>Zaman</th></tr>`;
    logs.forEach((log) => {
      html += `<tr>
        <td>${log.msisdn}</td>
        <td>${log.message}</td>
        <td>${log.usage_type}</td>
        <td>${log.notification_message}</td>
        <td>${new Date(log.timestamp).toLocaleString("tr-TR")}</td>
      </tr>`;
    });
    html += "</table></body></html>";
    res.send(html);
  } catch (err) {
    res.status(500).send("<p>Loglar okunamadı.</p>");
  }
});

export default router;
