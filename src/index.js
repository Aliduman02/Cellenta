import dotenv from "dotenv";
dotenv.config();
import express from "express";
import bodyParser from "body-parser";
import cors from "cors";
import smsLogRoutes from "./routes/smsLog.js";
import path from "path";
import { fileURLToPath } from "url";

const app = express();
app.use(cors());
app.use(bodyParser.json());

const __dirname = path.dirname(fileURLToPath(import.meta.url));
app.use("/api", smsLogRoutes);
app.use(express.static(path.join(__dirname, "../ui/public")));
app.listen(3000, "0.0.0.0", () => {
  console.log("🚀 Mock SMS servisi 3000 portunda çalışıyor.");
});
