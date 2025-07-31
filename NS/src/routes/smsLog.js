import { error, log } from "console";
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

router.delete("/sms-logs/:msisdn", async (req, res) => {
  const logPath = path.join("logs", "mock-sms-log.json");
  const msisdn = req.params.msisdn;

  try {
    const data = await fs.readFile(logPath, "utf-8");
    let logs = JSON.parse(data);

    const newLogs = logs.filter((log) => log.msisdn !== msisdn);

    if (newLogs.length === logs.length) {
      return res.status(404).json({ error: "Log bulunamadı." });
    }

    await fs.writeFile(logPath, JSON.stringify(newLogs, null, 2), "utf-8");
    res.status(200).json({ message: "Log silindi." });
  } catch (err) {
    console.error("Silme hatası:", err.message);
    res.status(500).json({ error: "Bir hata oluştu." });
  }
});

router.get("/sms-logs/test", async (req, res) => {
  res.status(200).json({
    success: true,
  });
});
export default router;
