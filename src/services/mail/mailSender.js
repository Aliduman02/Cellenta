import nodemailer from "nodemailer";
import dotenv from "dotenv";
dotenv.config();
import { fileURLToPath } from "url";
import path from "path";
import { callLogNotification } from "../db/log_notification.js";
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
import generateEmailTemplate from "../mail/emailTemplate.js";
import chalk from "chalk";
import { log } from "console";

const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST,
  port: Number(process.env.SMTP_PORT),
  secure: process.env.SMTP_SECURE === "true",
  auth: {
    user: process.env.SMTP_USER,
    pass: process.env.SMTP_PASS,
  },
});

export async function sendEmail({ to, parsed }) {
  try {
    const html = generateEmailTemplate(parsed);
    const info = await transporter.sendMail({
      from: process.env.SMTP_FROM,
      to,
      subject: "Kullanım Bilgilendirmesi - Cellenta",
      html: html || "İçerik yok",
      attachments: [
        {
          filename: "logo.png",
          path: path.join(__dirname, "../../img/cellenta-logo.png"),
          cid: "cellenta-logo",
        },
      ],
    });
    const logtype = parsed.usage_type + " " + parsed.notification_message;
    const logStatus = await callLogNotification(parsed.email, logtype);
    console.log(
      chalk.yellow(
        "Email Gönderildi:",
        JSON.stringify({
          success: true,
          emailId: info.messageId,
          logStatus,
        }),
        null,
        2
      )
    );
  } catch (error) {
    console.error(chalk.red("Email gönderme hatası:", error));
  }
}
