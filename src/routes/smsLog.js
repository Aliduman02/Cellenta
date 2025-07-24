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

export default router;
