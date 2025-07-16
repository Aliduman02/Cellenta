import dotenv from "dotenv";
dotenv.config();

import { startConsumer } from "./kafka/consumer.js";

//callGetCustomer(5551234567);
startConsumer().catch(console.error);
