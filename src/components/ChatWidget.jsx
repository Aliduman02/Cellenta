import React, { useState, useRef, useEffect } from "react";
import apiService, { sendGeminiMessage } from "../services/api";

// Çok dilli başlangıç mesajları
const getWelcomeMessage = (language) => {
  const messages = {
    'tr': "Merhaba! Ben Cellenta Bot, Cellenta Online Charging System için özel asistanınızım. Hesap girişi, paket seçimi, kalan kullanım sorguları ve fatura işlemleri konularında size yardımcı olabilirim. Size nasıl yardımcı olabilirim? 😊",
    'en': "Hello! I'm Cellenta Bot, your dedicated assistant for the Cellenta Online Charging System. I can help you with account access, package selection, usage queries, and billing operations. How can I help you today? 😊",
    'ar': "مرحباً! أنا Cellenta Bot، مساعدك المخصص لنظام Cellenta للشحن الإلكتروني. يمكنني مساعدتك في الوصول إلى الحساب واختيار الباقات واستعلامات الاستخدام وعمليات الفوترة. كيف يمكنني مساعدتك اليوم؟ 😊",
    'fr': "Bonjour! Je suis Cellenta Bot, votre assistant dédié pour le système de facturation en ligne Cellenta. Je peux vous aider avec l'accès au compte, la sélection de forfaits, les requêtes d'utilisation et les opérations de facturation. Comment puis-je vous aider aujourd'hui? 😊",
    'de': "Hallo! Ich bin Cellenta Bot, Ihr persönlicher Assistent für das Cellenta Online-Abrechnungssystem. Ich kann Ihnen bei Kontozugang, Paketauswahl, Nutzungsanfragen und Abrechnungsoperationen helfen. Wie kann ich Ihnen heute helfen? 😊",
    'es': "¡Hola! Soy Cellenta Bot, tu asistente dedicado para el sistema de facturación en línea Cellenta. Puedo ayudarte con el acceso a la cuenta, selección de paquetes, consultas de uso y operaciones de facturación. ¿Cómo puedo ayudarte hoy? 😊",
    'it': "Ciao! Sono Cellenta Bot, il tuo assistente dedicato per il sistema di fatturazione online Cellenta. Posso aiutarti con l'accesso all'account, la selezione di pacchetti, le query di utilizzo e le operazioni di fatturazione. Come posso aiutarti oggi? 😊",
    'ru': "Привет! Я Cellenta Bot, ваш персональный ассистент для системы онлайн-биллинга Cellenta. Я могу помочь с доступом к аккаунту, выбором пакетов, запросами использования и операциями биллинга. Как я могу вам помочь сегодня? 😊",
    'pt': "Olá! Eu sou o Cellenta Bot, seu assistente dedicado para o sistema de cobrança online Cellenta. Posso ajudá-lo com acesso à conta, seleção de pacotes, consultas de uso e operações de faturamento. Como posso ajudá-lo hoje? 😊",
    'nl': "Hallo! Ik ben Cellenta Bot, uw toegewijde assistent voor het Cellenta Online Facturatiesysteem. Ik kan u helpen met accounttoegang, pakketelectie, gebruiksvragen en factureringsoperaties. Hoe kan ik u vandaag helpen? 😊",
    'zh': "你好！我是 Cellenta Bot，您专属的 Cellenta 在线计费系统助手。我可以帮助您进行账户访问、套餐选择、使用查询和计费操作。今天我能为您做些什么？😊",
    'ja': "こんにちは！私はCellenta Bot、Cellentaオンライン課金システム専用のアシスタントです。アカウントアクセス、パッケージ選択、使用量クエリ、課金操作についてお手伝いできます。今日はいかがお手伝いしましょうか？😊",
    'ko': "안녕하세요! 저는 Cellenta 온라인 과금 시스템 전용 어시스턴트인 Cellenta Bot입니다. 계정 액세스, 패키지 선택, 사용량 조회, 청구 작업에 도움을 드릴 수 있습니다. 오늘 어떻게 도와드릴까요? 😊"
  };

  return messages[language] || messages['en']; // 기본값은 영어
};

// Çok dilli tooltip mesajları
const getTooltipMessage = (language) => {
  const tooltips = {
    'tr': "💬 Cellenta Asistanına Sor",
    'en': "💬 Ask Cellenta Assistant", 
    'ar': "💬 اسأل مساعد Cellenta",
    'fr': "💬 Demander à l'Assistant Cellenta",
    'de': "💬 Cellenta Assistenten fragen",
    'es': "💬 Pregunta al Asistente Cellenta",
    'it': "💬 Chiedi all'Assistente Cellenta",
    'ru': "💬 Спросить Cellenta Ассистента",
    'pt': "💬 Perguntar ao Assistente Cellenta",
    'nl': "💬 Vragen aan Cellenta Assistent",
    'zh': "💬 询问 Cellenta 助手",
    'ja': "💬 Cellenta アシスタントに聞く",
    'ko': "💬 Cellenta 어시스턴트에게 문의"
  };

  return tooltips[language] || tooltips['en'];
};

// Çok dilli placeholder ve buton metinleri
const getPlaceholderText = (language) => {
  const placeholders = {
    'tr': "Mesajınızı yazın...",
    'en': "Type your message...",
    'ar': "اكتب رسالتك...",
    'fr': "Tapez votre message...",
    'de': "Geben Sie Ihre Nachricht ein...",
    'es': "Escribe tu mensaje...",
    'it': "Scrivi il tuo messaggio...",
    'ru': "Введите ваше сообщение...",
    'pt': "Digite sua mensagem...",
    'nl': "Typ uw bericht...",
    'zh': "输入您的消息...",
    'ja': "メッセージを入力してください...",
    'ko': "메시지를 입력하세요..."
  };
  return placeholders[language] || placeholders['en'];
};

const getSendButtonText = (language) => {
  const buttons = {
    'tr': "Gönder",
    'en': "Send",
    'ar': "إرسال",
    'fr': "Envoyer", 
    'de': "Senden",
    'es': "Enviar",
    'it': "Invia",
    'ru': "Отправить",
    'pt': "Enviar",
    'nl': "Verzenden",
    'zh': "发送",
    'ja': "送信",
    'ko': "보내기"
  };
  return buttons[language] || buttons['en'];
};

const getTypingText = (language) => {
  const typing = {
    'tr': "Yazıyor...",
    'en': "Typing...",
    'ar': "يكتب...",
    'fr': "Tape...",
    'de': "Tippt...",
    'es': "Escribiendo...",
    'it': "Sta scrivendo...",
    'ru': "Печатает...",
    'pt': "Digitando...",
    'nl': "Aan het typen...",
    'zh': "正在输入...",
    'ja': "入力中...",
    'ko': "입력 중..."
  };
  return typing[language] || typing['en'];
};

// 브라우저 언어 감지 함수
const detectBrowserLanguage = () => {
  const browserLang = navigator.language || navigator.userLanguage || 'en';
  const langCode = browserLang.split('-')[0].toLowerCase(); // 'en-US' -> 'en'
  
  // 지원하는 언어 목록
  const supportedLanguages = ['tr', 'en', 'ar', 'fr', 'de', 'es', 'it', 'ru', 'pt', 'nl', 'zh', 'ja', 'ko'];
  
  return supportedLanguages.includes(langCode) ? langCode : 'en';
};

export default function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [showTooltip, setShowTooltip] = useState(false);
  const [userLanguage] = useState(detectBrowserLanguage());
  const [messages, setMessages] = useState([
    {
      id: 1,
      text: getWelcomeMessage(userLanguage),
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
    width: isDesktop ? "100px" : "82px",
    height: isDesktop ? "100px" : "82px",
    padding: isDesktop ? "10px" : "8px"
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
            width: isDesktop ? "42px" : "36px",
            height: isDesktop ? "42px" : "36px",
            transition: "transform 0.3s ease"
          }}
        />
        
                  {/* Tooltip - only show on desktop */}
        {showTooltip && isDesktop && (
          <div style={{
            position: "absolute",
            right: isDesktop ? "75px" : "65px",
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
            {getTooltipMessage(userLanguage)}
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
                  {getTypingText(userLanguage)}
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
              placeholder={getPlaceholderText(userLanguage)}
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
              {getSendButtonText(userLanguage)}
            </button>
          </form>
        </div>
      )}
    </>
  );
}