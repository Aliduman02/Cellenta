import dotenv from "dotenv";
dotenv.config();
import pkg from "kafkajs";
const { Kafka, CompressionTypes, CompressionCodecs } = pkg;
import snappy from "kafkajs-snappy";
CompressionCodecs[CompressionTypes.Snappy] = snappy;
import { sendEmail } from "../services/mail/mailSender.js";
import { callGetCustomer } from "../services/db/get_customer.js";
const kafka = new Kafka({
  clientId: "notification-service",
  brokers: [process.env.KAFKA_BROKER],
});
const consumer = kafka.consumer({ groupId: "notification-group" });
CompressionCodecs[CompressionTypes.Snappy] = snappy;

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
      } else if (typeof message.value !== "object") {
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
        const msisdn = parsed.customer.msisdn;
        const customerResult = await callGetCustomer(msisdn);

        if (customerResult.rows.length > 0) {
          const row = customerResult.rows[0];
          parsed.customer.name = row.NAME;
          parsed.customer.surname = row.SURNAME;
          parsed.customer.email = row.EMAIL;
        } else {
          console.log("Müşteri bulunamadı.");
        }
        await sendEmail({
          to: parsed.customer.email,
          parsed: parsed,
        });
      } catch (error) {
        console.error("❌ JSON parse hatası:", error);
      }
    },
  });
}
