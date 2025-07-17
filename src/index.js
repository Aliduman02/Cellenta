import dotenv from "dotenv";
dotenv.config();

import { startConsumer } from "./kafka/consumer.js";
startConsumer().catch(console.error);
