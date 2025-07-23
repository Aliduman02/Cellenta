import React, { useState, useRef, useEffect } from "react";
import apiService, { sendGeminiMessage } from "../services/api";

export default function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [showTooltip, setShowTooltip] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      text: "Merhaba, ben Cellenta Asistanı! Size nasıl yardımcı olabilirim?",
      sender: "bot",
      timestamp: new Date()
    }
  ]);
  const [inputMessage, setInputMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isDesktop, setIsDesktop] = useState(false);
  const messagesEndRef = useRef(null);

  // Responsive check
  useEffect(() => {
    const checkScreenSize = () => {
      setIsDesktop(window.innerWidth >= 768);
    };

    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

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
        /ne.*(kadar|kaldı)/i.test(message) ||
        /kullanım.*(sorgula|bilgi)/i.test(message)
      ) {
        const usage = await apiService.getUsageData();
        let usageText;
        if (
          usage.remainingData === 0 &&
          usage.remainingMinutes === 0 &&
          usage.remainingSms === 0
        ) {
          usageText = "Kalan kullanımınız bulunmuyor, tüm haklarınız tükenmiş görünüyor.";
        } else {
          usageText = `Kalan internet: ${usage.remainingData} MB, dakika: ${usage.remainingMinutes}, SMS: ${usage.remainingSms}`;
        }
        // Her zaman Türkçe yanıt ver
        const prompt = `Kalan kullanımım: ${usageText}. Lütfen bunu kullanıcıya Türkçe açıkla ve ayrıntılı bilgi için ana sayfaya gidebileceğini belirt.`;
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
        text: "Üzgünüm, şu anda bağlantı sorunu yaşıyorum. Lütfen daha sonra tekrar deneyin.",
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

  // Responsive styles
  const getModalStyles = () => {
    if (isDesktop) {
      return {
        position: "fixed",
        bottom: 100,
        right: 32,
        width: 380,
        height: 520,
        maxHeight: "calc(100vh - 140px)"
      };
    } else {
      // Mobile and tablet
      return {
        position: "fixed",
        bottom: 80,
        right: 16,
        left: 16,
        width: "auto",
        height: 450,
        maxHeight: "calc(100vh - 120px)",
        maxWidth: 400
      };
    }
  };

  const getButtonStyles = () => ({
    width: isDesktop ? "50px" : "45px",
    height: isDesktop ? "50px" : "45px",
    padding: isDesktop ? "8px" : "6px"
  });

  return (
    <>
      {/* Chat Widget Button */}
      <div 
        className="chat-widget"
        onClick={() => setIsOpen(!isOpen)}
        onMouseEnter={() => setShowTooltip(true)}
        onMouseLeave={() => setShowTooltip(false)}
        style={{ 
          cursor: "pointer",
          borderRadius: "50%",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          position: "relative",
          ...getButtonStyles()
        }}
      >
        <img 
          src={`${process.env.PUBLIC_URL}/images/icon2.png`} 
          alt="Cellenta Bot" 
          className="chat-widget-logo"
          style={{
            width: isDesktop ? "36px" : "30px",
            height: isDesktop ? "36px" : "30px",
            transition: "transform 0.3s ease"
          }}
        />
        
        {/* Tooltip - only show on desktop */}
        {showTooltip && isDesktop && (
          <div style={{
            position: "absolute",
            right: "65px",
            top: "50%",
            transform: "translateY(-50%)",
            background: "rgba(31, 41, 55, 0.95)",
            color: "#fff",
            padding: "8px 12px",
            borderRadius: "8px",
            fontSize: "14px",
            fontWeight: "500",
            whiteSpace: "nowrap",
            boxShadow: "0 4px 12px rgba(0,0,0,0.15)",
            zIndex: 1001,
            animation: "fadeIn 0.2s ease"
          }}>
            💬 Cellenta Asistanına Sor
            {/* Arrow */}
            <div style={{
              position: "absolute",
              right: "-5px",
              top: "50%",
              transform: "translateY(-50%)",
              width: 0,
              height: 0,
              borderLeft: "5px solid rgba(31, 41, 55, 0.95)",
              borderTop: "5px solid transparent",
              borderBottom: "5px solid transparent"
            }} />
          </div>
        )}
      </div>

      {/* Chat Modal */}
      {isOpen && (
        <div style={{
          ...getModalStyles(),
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
            padding: isDesktop ? "16px 20px" : "12px 16px",
            borderBottom: "1px solid #e5e7eb",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            background: "#f8fafc",
            borderRadius: "16px 16px 0 0"
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <img 
                src={`${process.env.PUBLIC_URL}/images/icon2.png`} 
                alt="Cellenta Bot" 
                style={{ 
                  width: isDesktop ? 24 : 20, 
                  height: isDesktop ? 24 : 20 
                }} 
              />
              <span style={{ 
                fontWeight: 600, 
                color: "#1f2937",
                fontSize: isDesktop ? "16px" : "14px"
              }}>
                Cellenta Assistant
              </span>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              style={{
                background: "none",
                border: "none",
                fontSize: isDesktop ? 18 : 16,
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
            padding: isDesktop ? "16px" : "12px",
            display: "flex",
            flexDirection: "column",
            gap: isDesktop ? 12 : 10
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
                  maxWidth: isDesktop ? "80%" : "85%",
                  padding: isDesktop ? "12px 16px" : "10px 14px",
                  borderRadius: 16,
                  background: message.sender === "user" ? "#7c3aed" : "#f3f4f6",
                  color: message.sender === "user" ? "#fff" : "#1f2937",
                  fontSize: isDesktop ? 14 : 13,
                  lineHeight: 1.4,
                  wordBreak: "break-word"
                }}>
                  {message.text}
                </div>
              </div>
            ))}
            {isLoading && (
              <div style={{ display: "flex", justifyContent: "flex-start" }}>
                <div style={{
                  padding: isDesktop ? "12px 16px" : "10px 14px",
                  borderRadius: 16,
                  background: "#f3f4f6",
                  color: "#6b7280",
                  fontSize: isDesktop ? 14 : 13
                }}>
                  Typing...
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <form onSubmit={handleSubmit} style={{
            padding: isDesktop ? "16px" : "12px",
            borderTop: "1px solid #e5e7eb",
            display: "flex",
            gap: isDesktop ? 8 : 6
          }}>
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="Mesajınızı yazın..."
              disabled={isLoading}
              style={{
                flex: 1,
                padding: isDesktop ? "12px 16px" : "10px 14px",
                border: "1px solid #d1d5db",
                borderRadius: 20,
                fontSize: isDesktop ? 14 : 13,
                outline: "none"
              }}
            />
            <button
              type="submit"
              disabled={isLoading || !inputMessage.trim()}
              style={{
                padding: isDesktop ? "12px 16px" : "10px 14px",
                background: "#7c3aed",
                color: "#fff",
                border: "none",
                borderRadius: 20,
                cursor: "pointer",
                fontSize: isDesktop ? 14 : 13,
                fontWeight: 600,
                opacity: (isLoading || !inputMessage.trim()) ? 0.5 : 1,
                minWidth: isDesktop ? "auto" : "60px"
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