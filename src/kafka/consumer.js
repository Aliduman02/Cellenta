import dotenv from "dotenv";
dotenv.config();
import pkg from "kafkajs";
import snappy from "kafkajs-snappy";
import { sendEmail } from "../services/mailSender.js";
import generateEmailTemplate from "../services/emailTemplate.js";

CompressionCodecs[CompressionTypes.Snappy] = snappy;
const kafka = new Kafka({
  clientId: "notification-service",
  brokers: [process.env.KAFKA_BROKER],
});
const { Kafka, CompressionTypes, CompressionCodecs } = pkg;
const consumer = kafka.consumer({ groupId: "notification-group" });

export async function startConsumer() {
  await consumer.connect();
  await consumer.subscribe({
    topic: process.env.KAFKA_TOPIC,
    fromBeginning: true,
  });
  console.log("✅ Kafka consumer dinleniyor...");
  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      if (!message.value) {
        console.warn("⚠️ Mesajın içeriği boş!");
        return;
      }
      if (typeof message.value !== "object") {
        console.warn("Beklenmeyen mesaj tipi:", typeof message.value);
      }
      const data = message.value?.toString();
      if (!data) {
        console.warn("⚠️ message.value boş!");
        return;
      }
      console.log(`📩 Mesaj geldi [${topic}] Partition: ${partition}`);
      try {
        const parsed = JSON.parse(data);
        const html = generateEmailTemplate(parsed);
        await sendEmail({
          to: parsed.customer.email,
          body: html || "İçerik yok",
        });
      } catch (error) {
        console.error("❌ JSON parse hatası:", err.message);
      }
    },
  });
}
