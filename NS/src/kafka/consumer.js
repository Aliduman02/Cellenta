import dotenv from "dotenv";
dotenv.config();
import chalk from "chalk";
import { spawn } from "child_process";
import { sendEmail } from "../services/mail/mailSender.js";
import { callGetCustomer } from "../services/db/get_customer.js";
import { smsSender } from "../services/sms/smsSender.js";
const jarProcess = spawn("java", [
  "-jar",
  "./KafkaApp-1.0-SNAPSHOT.jar",
  "--topic=chf-to-notification",
  console.log(
    chalk.greenBright(
      "*******************************  Jarprocess Çalışıyor  *******************************"
    )
  ),
]);
async function handleMessage(parsed) {
  try {
    const customerResult = await callGetCustomer(parsed.msisdn);
    if (customerResult.rows.length > 0) {
      const row = customerResult.rows[0];
      parsed.name = row.NAME;
      parsed.surname = row.SURNAME;
      parsed.email = row.EMAIL;
      console.log(chalk.green("===================================="));
      console.log(chalk.blue("parsed:", JSON.stringify(parsed, null, 2)));
      await sendEmail({
        to: parsed.email,
        parsed: parsed,
      });
      await smsSender({
        to: parsed.msisdn,
        parsed: parsed,
      });
    } else {
      console.log(chalk.yellow("⚠ Müşteri bulunamadı. msisdn:", parsed.msisdn));
    }
  } catch (error) {
    console.log(chalk.red("handlemessage hatası:", error.message));
  }
}

jarProcess.stdout.on("data", async (data) => {
  const output = data.toString();
  const regex = /NotificationMessage\{(.+?)\}/;
  const match = output.match(regex);

  if (match) {
    const fields = match[1]
      .split(", ")
      .map((f) => f.split("="))
      .reduce((acc, [key, value]) => {
        acc[key.trim()] = value?.trim()?.replace(/^'|'$/g, "");
        return acc;
      }, {});

    const parsed = {
      timestamp: fields.timestamp,
      msisdn: fields.msisdn,
      usage_type: fields.usageType,
      percentage: Number(fields.percentage),
      notification_message: fields.notificationMessage,
      package_name: fields.packageName,
      package_start_date: fields.packageStartDate,
      package_end_date: fields.packageEndDate,
      total_minutes: Number(fields.totalMinutes),
      total_sms: Number(fields.totalSms),
      total_data: Number(fields.totalData),
      remaining_minutes: Number(fields.remainingMinutes),
      remaining_sms: Number(fields.remainingSms),
      remaining_data: Number(fields.remainingData),
    };
    try {
      await handleMessage(parsed);
    } catch (err) {
      console.error(chalk.red("🚨Mesaj İşleme hatası:", err.message));
    }
  }
});

jarProcess.stderr.on("data", (data) => {
  console.error(chalk.red("❌ JAR std hatası:", data.toString()));
});

jarProcess.on("close", (code) => {
  console.log(chalk.blue(`🔚 JAR process kapandı. Kod: ${code}`));
});

jarProcess.on("error", (err) => {
  console.error(chalk.red.bold("💥 JAR process başlatılamadı:", err.message));
});
