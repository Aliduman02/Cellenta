import dotenv from "dotenv";
dotenv.config();
import express from "express";
import smsRouter from "./routes/sms.js";
import bodyParser from "body-parser";
import cors from "cors";
import smsLogRoutes from "./routes/smsLog.js";

const app = express();
app.use(cors());
app.use(bodyParser.json());

app.use("/api", smsLogRoutes);
app.use("/api", smsRouter);
app.listen(3000, "0.0.0.0", () => {
  console.log("🚀 Mock SMS servisi 3000 portunda çalışıyor.");
});
