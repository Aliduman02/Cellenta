require("dotenv").config();
const { Kafka } = require("kafkajs");

const kafka = new Kafka({
  clientId: "notification-service",
  brokers: [process.env.KAFKA_BROKER],
});

const consumer = kafka.consumer({ groupId: "notification-group" });

async function startConsumer() {
  await consumer.connect();
  await consumer.subscribe({
    topic: process.env.KAFKA_TOPIC,
    fromBeginning: true,
  });

  console.log("✅ Kafka consumer dinleniyor...");

  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      console.log(`📩 Mesaj geldi [${topic}] Partition: ${partition}`);
      console.log(`Mesaj içeriği: ${message.value.toString()}`);
    },
  });
}

module.exports = { startConsumer };
