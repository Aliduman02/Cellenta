import nodemailer from "nodemailer";
import dotenv from "dotenv";
dotenv.config();
import { fileURLToPath } from "url";
import { dirname } from "path";
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST,
  port: Number(process.env.SMTP_PORT),
  secure: process.env.SMTP_SECURE === "true",
  auth: {
    user: process.env.SMTP_USER,
    pass: process.env.SMTP_PASS,
  },
});

export async function sendEmail({ to, body }) {
  try {
    const info = await transporter.sendMail({
      from: process.env.SMTP_FROM,
      to,
      subject: "Kullanım Bilgilendirmesi - Cellenta",
      html: body,
      attachments: [
        {
          filename: "logo.png",
          path: __dirname + "/../public/assets/cellenta-logo.png",
          cid: "cellenta-logo",
        },
      ],
    });
    console.log("Email gönderildi:", info.messageId);
  } catch (error) {
    console.error("Email gönderme hatası:", error);
  }
}
