import express from "express";
import os from "os";
import checkDiskSpace from "check-disk-space";
const router = express.Router();
import pidusage from "pidusage";

const SERVICE_NAME = "Notification Service";
const startTime = new Date();

const getTimestamp = () =>
  new Date().toLocaleString("tr-TR", { timeZone: "Europe/Istanbul" });

function formatUptime(start) {
  const diff = (new Date() - start) / 1000;
  const days = Math.floor(diff / (60 * 60 * 24));
  const hours = Math.floor((diff / (60 * 60)) % 24);
  const minutes = Math.floor((diff / 60) % 60);
  const seconds = Math.floor(diff % 60);
  return `${days > 0 ? `${days}d ` : ""}${hours}h ${minutes}m ${seconds}s`;
}

async function getSystemMetrics() {
  let cpuPercent = -1;
  try {
    const stats = await pidusage(process.pid);
    cpuPercent = Math.round(stats.cpu * 10) / 10;
  } catch (error) {
    console.error("CPU ölçüm hatası:", error.message);
  }

  // RAM kullanımı
  const totalMemory = os.totalmem();
  const freeMemory = os.freemem();
  const usedMemory = totalMemory - freeMemory;
  const memoryPercent = Math.round((usedMemory / totalMemory) * 1000) / 10;

  // Disk kullanımı
  const diskPath = os.platform() === "win32" ? "C:" : "/";

  let diskPercent = -1;
  try {
    const disk = await checkDiskSpace(diskPath);
    const used = disk.size - disk.free;
    diskPercent = Math.round((used / disk.size) * 1000) / 10;
  } catch {
    diskPercent = -1;
  }

  return {
    cpu_percent: cpuPercent,
    memory_percent: memoryPercent,
    disk_percent: diskPercent,
  };
}

router.get("/NS/health", async (req, res) => {
  try {
    const metrics = await getSystemMetrics();

    const data = {
      status: 200,
      service: SERVICE_NAME,
      ...metrics,
      jar_status: "RUNNING",
      uptime: formatUptime(startTime),
      timestamp: getTimestamp(),
    };

    console.log("✅ Health check OK:", data);
    res.status(200).json(data);
  } catch (e) {
    console.error("❌ Health check error:", e.message);
    res.status(503).json({
      status: 503,
      service: SERVICE_NAME,
      jar_status: "ERROR",
      error: e.message,
      timestamp: getTimestamp(),
    });
  }
});

export default router;
