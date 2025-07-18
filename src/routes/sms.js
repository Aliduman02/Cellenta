import express from "express";
import { smsSender } from "../services/sms/smsSender.js";

const router = express.Router();

router.post("/send-sms", (req, res) => {
  const { to, message } = req.body;

  if (!to || !message) {
    return res
      .status(400)
      .json({ error: "'to' ve 'message' alanları zorunludur." });
  }

  const result = smsSender({ to, message });
  res.status(200).json(result);
});

export default router;
