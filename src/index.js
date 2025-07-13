require("dotenv").config();
const { startConsumer } = require("./kafka/consumer");

console.log("Kafka Broker:", process.env.KAFKA_BROKER);
console.log("Kafka Topic:", process.env.KAFKA_TOPIC);

startConsumer();
