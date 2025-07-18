import dotenv from "dotenv";
dotenv.config();
import { startConsumer } from "./kafka/consumer.js";
import express from "express";
import smsRouter from "./routes/sms.js";
import bodyParser from "body-parser";

const app = express();
app.use(bodyParser.json());
app.use("/api", smsRouter);
app.listen(3000, () => {
  console.log("🚀 Mock SMS servisi 3000 portunda çalışıyor.");
});
startConsumer().catch(console.error);
