import nodemailer from "nodemailer";
import dotenv from "dotenv";
dotenv.config();
import { fileURLToPath } from "url";
import path from "path";
import { callLogNotification } from "../db/log_notification.js";
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
import generateEmailTemplate from "../mail/emailTemplate.js";

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
          path: path.join(__dirname, "../../public/cellenta-logo.png"),
          cid: "cellenta-logo",
        },
      ],
    });
    console.log("Email gönderildi:", info.messageId);
    const logtype = parsed.usage_type + " " + parsed.notification_message;
    const logStatus = await callLogNotification(parsed.email, logtype);
    console.log("Email Log Notification Status:", logStatus);
  } catch (error) {
    console.error("Email gönderme hatası:", error);
  }
}
