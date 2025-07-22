import React, { useState, useRef, useEffect } from "react";
import apiService, { sendGeminiMessage } from "../services/api";

export default function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      text: "Hello, this is Cellenta! How can I help you?",
      sender: "bot",
      timestamp: new Date()
    }
  ]);
  const [inputMessage, setInputMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleUsageQuery = async () => {
    try {
      const usage = await apiService.getUsageData();
      let usageText = `Kalan internet: ${usage.remainingData} MB, dakika: ${usage.remainingMinutes}, SMS: ${usage.remainingSms}`;
      if (
        usage.remainingData === 0 &&
        usage.remainingMinutes === 0 &&
        usage.remainingSms === 0
      ) {
        usageText = "Kalan kullanımınız yok, tüm haklarınız bitmiş görünüyor.";
      }
      return usageText;
    } catch (e) {
      return "Kalan kullanım bilgisi alınamadı.";
    }
  };

  const sendMessage = async (message) => {
    if (!message.trim()) return;

    // Kullanıcı mesajını ekle
    const userMessage = {
      id: Date.now(),
      text: message,
      sender: "user",
      timestamp: new Date()
    };
    setMessages(prev => [...prev, userMessage]);
    setInputMessage("");
    setIsLoading(true);

    try {
      let botResponse;
      // Kalan kullanım sorgusu mu?
      if (
        /kalan.*(kullanım|internet|dakika|sms|bakiye)/i.test(message) ||
        /usage|balance|remaining/i.test(message)
      ) {
        const usage = await apiService.getUsageData();
        let usageText;
        if (
          usage.remainingData === 0 &&
          usage.remainingMinutes === 0 &&
          usage.remainingSms === 0
        ) {
          usageText = "You have no remaining usage, all your allowances appear to be used up.";
        } else {
          usageText = `Remaining data: ${usage.remainingData} MB, minutes: ${usage.remainingMinutes}, SMS: ${usage.remainingSms}`;
        }
        // Kullanıcı mesajı İngilizce mi Türkçe mi kontrol et
        const isEnglish = /[a-z]/i.test(message) && !/[çğıöşü]/i.test(message);
        const prompt = isEnglish
          ? `My current usage is: ${usageText}. Please explain this to the user in English and also tell them they can see more details on the home page.`
          : `Kalan kullanımım: ${usageText}. Lütfen bunu kullanıcıya Türkçe açıkla ve ayrıntılı bilgi için ana sayfaya gidebileceğini belirt.`;
        botResponse = await sendGeminiMessage(prompt);
      } else {
        // Gemini API'den gerçek cevap al
        botResponse = await sendGeminiMessage(message);
      }

      // Bot yanıtını ekle
      const botMessage = {
        id: Date.now() + 1,
        text: botResponse,
        sender: "bot",
        timestamp: new Date()
      };
      setMessages(prev => [...prev, botMessage]);
    } catch (error) {
      console.error("API Error:", error);
      const errorMessage = {
        id: Date.now() + 1,
        text: "I'm sorry, I'm having trouble connecting right now. Please try again later.",
        sender: "bot",
        timestamp: new Date()
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    sendMessage(inputMessage);
  };

  return (
    <>
      {/* Chat Widget Button */}
      <div 
        className="chat-widget"
        onClick={() => setIsOpen(!isOpen)}
        style={{ cursor: "pointer" }}
      >
        <img src={`${process.env.PUBLIC_URL}/images/icon2.png`} alt="Cellenta Bot" className="chat-widget-logo" />
        <div className="chat-widget-message">
          <strong>Hello, this is Cellenta!</strong>
          <br />
          How can I help you?
        </div>
      </div>

      {/* Chat Modal */}
      {isOpen && (
        <div style={{
          position: "fixed",
          bottom: 100,
          right: 32,
          width: 350,
          height: 500,
          background: "#fff",
          borderRadius: 16,
          boxShadow: "0 8px 32px rgba(0,0,0,0.12)",
          display: "flex",
          flexDirection: "column",
          zIndex: 1000,
          border: "1px solid #e5e7eb"
        }}>
          {/* Header */}
          <div style={{
            padding: "16px 20px",
            borderBottom: "1px solid #e5e7eb",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            background: "#f8fafc"
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <img src={`${process.env.PUBLIC_URL}/images/icon2.png`} alt="Cellenta Bot" style={{ width: 24, height: 24 }} />
              <span style={{ fontWeight: 600, color: "#1f2937" }}>Cellenta Assistant</span>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              style={{
                background: "none",
                border: "none",
                fontSize: 18,
                cursor: "pointer",
                color: "#6b7280",
                padding: 4
              }}
            >
              ×
            </button>
          </div>

          {/* Messages */}
          <div style={{
            flex: 1,
            overflowY: "auto",
            padding: "16px",
            display: "flex",
            flexDirection: "column",
            gap: 12
          }}>
            {messages.map((message) => (
              <div
                key={message.id}
                style={{
                  display: "flex",
                  justifyContent: message.sender === "user" ? "flex-end" : "flex-start"
                }}
              >
                <div style={{
                  maxWidth: "80%",
                  padding: "12px 16px",
                  borderRadius: 16,
                  background: message.sender === "user" ? "#7c3aed" : "#f3f4f6",
                  color: message.sender === "user" ? "#fff" : "#1f2937",
                  fontSize: 14,
                  lineHeight: 1.4
                }}>
                  {message.text}
                </div>
              </div>
            ))}
            {isLoading && (
              <div style={{ display: "flex", justifyContent: "flex-start" }}>
                <div style={{
                  padding: "12px 16px",
                  borderRadius: 16,
                  background: "#f3f4f6",
                  color: "#6b7280",
                  fontSize: 14
                }}>
                  Typing...
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <form onSubmit={handleSubmit} style={{
            padding: "16px",
            borderTop: "1px solid #e5e7eb",
            display: "flex",
            gap: 8
          }}>
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="Type your message..."
              disabled={isLoading}
              style={{
                flex: 1,
                padding: "12px 16px",
                border: "1px solid #d1d5db",
                borderRadius: 20,
                fontSize: 14,
                outline: "none"
              }}
            />
            <button
              type="submit"
              disabled={isLoading || !inputMessage.trim()}
              style={{
                padding: "12px 16px",
                background: "#7c3aed",
                color: "#fff",
                border: "none",
                borderRadius: 20,
                cursor: "pointer",
                fontSize: 14,
                fontWeight: 600,
                opacity: (isLoading || !inputMessage.trim()) ? 0.5 : 1
              }}
            >
              Send
            </button>
          </form>
        </div>
      )}
    </>
  );
}