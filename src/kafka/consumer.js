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
import { smsSender } from "../services/sms/smsSender.js";

const consumer = kafka.consumer({ groupId: "notification-group" });
CompressionCodecs[CompressionTypes.Snappy] = snappy;

export async function startConsumer() {
  await consumer.connect();
  await consumer.subscribe({
    topic: process.env.KAFKA_TOPIC,
    fromBeginning: false,
  });
  console.log("✅ Kafka consumer dinleniyor...");
  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      try {
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
        console.log(`===================================================`);
        console.log(`📩 Mesaj geldi [${topic}] Partition: ${partition}`);
        try {
          const parsed = JSON.parse(data);
          const msisdn = parsed.msisdn;
          const customerResult = await callGetCustomer(msisdn);
          if (!(customerResult.rows.length === 0)) {
            const row = customerResult.rows[0];
            parsed.name = row.NAME;
            parsed.surname = row.SURNAME;
            parsed.email = row.EMAIL;
            console.log("email:", parsed.email);
            console.log("parsed:", parsed);
            const emailSum = await sendEmail({
              to: parsed.email,
              parsed: parsed,
            });
            console.log(emailSum);
            const smsSum = await smsSender({
              to: parsed.msisdn,
              parsed: parsed,
            });
            console.log(smsSum);
            console.log("offset:", message.offset);
          } else {
            console.log("⚠ Müşteri bulunamadı. msisdn:", parsed.msisdn);
          }
        } catch (error) {
          console.error("❌ JSON parse hatası:", error.message);
        }
      } catch (error) {
        console.log("en tepe block hatası:", error.message);
      }
    },
  });
}
