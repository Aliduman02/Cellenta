import React, { useState, useEffect } from "react";
import apiService from "./services/api";
import { LeftPanel } from "./LeftPanel";
import "./LoginPage.css";

// Adım 1: Email giriši
function EmailStep({ onNext, onBack }) {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email) {
      setError("E-posta gerekli");
      return;
    }

    setIsLoading(true);
    try {
      await apiService.forgotPassword(email);
      onNext({ email });
    } catch (error) {
      console.error('Forgot password failed:', error);
      if (error.message.includes('404')) {
        setError("Bu e-posta adresi kayıtlı değil");
      } else {
        setError("Kod gönderilemedi. Lütfen tekrar deneyin.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Şifrenizi mi unuttunuz?</h3>
      <h2 className="form-title">Endişelenmeyin! Olur böyle şeyler.</h2>
      <p style={{ fontSize: 16, color: "#666", marginBottom: 32, textAlign: "center" }}>
        Lütfen hesabınızla ilişkili e-posta adresini girin.
      </p>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="email">E-posta</label>
          <input
            type="email"
            id="email"
            name="email"
            placeholder="E-posta adresinizi girin"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={error ? "error" : ""}
            disabled={isLoading}
          />
          {error && <div style={{ color: "#ef4444", fontSize: 14, marginTop: 8 }}>{error}</div>}
        </div>
        <button 
          type="submit" 
          className="login-button"
          disabled={isLoading}
          style={{ 
            opacity: isLoading ? 0.7 : 1,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: "8px"
          }}
        >
          {isLoading && (
            <div style={{
              width: "16px",
              height: "16px",
              border: "2px solid rgba(255,255,255,0.3)",
              borderTop: "2px solid #fff",
              borderRadius: "50%",
              animation: "spin 1s linear infinite"
            }}></div>
          )}
          {isLoading ? "Gönderiliyor..." : "Kod Gönder"}
        </button>
      </form>
      <div className="signup-prompt">
        Şifrenizi hatırladınız mı? <a href="/login">Giriş yapın</a>
      </div>
    </div>
  );
}

// Adım 2: Kod doğrulama
function CodeStep({ onNext, onBack, email }) {
  const [code, setCode] = useState(["", "", "", "", "", ""]);
  const [error, setError] = useState("");
  const [timeLeft, setTimeLeft] = useState(120); // 2 dakika
  const [canResend, setCanResend] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    } else {
      setCanResend(true);
    }
  }, [timeLeft]);

  const handleCodeChange = (index, value) => {
    if (value.length <= 1) {
      const newCode = [...code];
      newCode[index] = value;
      setCode(newCode);
      
      // Otomatik sonraki input'a geç
      if (value && index < 5) {
        document.getElementById(`code-${index + 1}`).focus();
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const fullCode = code.join("");
    if (fullCode.length !== 6) {
      setError("Lütfen 6 haneli kodu girin");
      return;
    }

    setIsLoading(true);
    try {
      await apiService.verifyCode(email, fullCode);
      onNext({ code: fullCode });
    } catch (error) {
      console.error('Code verification failed:', error);
      if (error.message.includes('400')) {
        setError("Geçersiz kod");
      } else {
        setError("Doğrulama başarısız. Lütfen tekrar deneyin.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    try {
      await apiService.forgotPassword(email);
      setTimeLeft(120);
      setCanResend(false);
      setCode(["", "", "", "", "", ""]);
      setError("");
    } catch (error) {
      console.error('Resend failed:', error);
      setError("Kod tekrar gönderilemedi. Lütfen tekrar deneyin.");
    }
  };

  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Lütfen e-postanızı kontrol edin</h3>
      <h2 className="form-title">E-postanıza bir kod gönderdik</h2>
      <form onSubmit={handleSubmit}>
        <div style={{ display: "flex", gap: 12, justifyContent: "center", marginBottom: 32 }}>
          {code.map((digit, index) => (
            <input
              key={index}
              id={`code-${index}`}
              type="text"
              maxLength={1}
              value={digit}
              onChange={(e) => handleCodeChange(index, e.target.value)}
              disabled={isLoading}
              style={{
                width: 50,
                height: 50,
                textAlign: "center",
                fontSize: 20,
                fontWeight: 600,
                border: "2px solid #e1e5e9",
                borderRadius: 8,
                outline: "none",
                transition: "border-color 0.2s",
              }}
              onFocus={(e) => (e.target.style.borderColor = "#7c3aed")}
              onBlur={(e) => (e.target.style.borderColor = "#e1e5e9")}
            />
          ))}
        </div>
        {error && <div style={{ color: "#ef4444", fontSize: 14, textAlign: "center", marginBottom: 16 }}>{error}</div>}
        <button 
          type="submit" 
          className="login-button"
          disabled={isLoading}
          style={{ 
            opacity: isLoading ? 0.7 : 1,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: "8px"
          }}
        >
          {isLoading && (
            <div style={{
              width: "16px",
              height: "16px",
              border: "2px solid rgba(255,255,255,0.3)",
              borderTop: "2px solid #fff",
              borderRadius: "50%",
              animation: "spin 1s linear infinite"
            }}></div>
          )}
          {isLoading ? "Doğrulanıyor..." : "Doğrula"}
        </button>
      </form>
      <div style={{ textAlign: "center", marginTop: 24, fontSize: 14, color: "#666" }}>
        {canResend ? (
          <button 
            onClick={handleResend} 
            style={{ 
              background: "none", 
              border: "none", 
              color: "#0072ff", 
              cursor: "pointer", 
              fontWeight: 600 
            }}
          >
            Kodu tekrar gönder
          </button>
        ) : (
          <span>Kodu tekrar gönder {Math.floor(timeLeft / 60)}:{(timeLeft % 60).toString().padStart(2, '0')}</span>
        )}
      </div>
    </div>
  );
}

// Adım 3: Yeni şifre
function ResetPasswordStep({ onNext, onBack, email, code }) {
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const validatePassword = (pass) => {
    const errors = {};
    if (pass.length < 8) errors.length = "En az 8 karakter olmalı";
    if (!/[A-Z]/.test(pass)) errors.uppercase = "En az bir büyük harf içermeli (A-Z)";
    if (!/[a-z]/.test(pass)) errors.lowercase = "En az bir küçük harf içermeli (a-z)";
    if (!/[0-9]/.test(pass)) errors.digit = "En az bir rakam içermeli (0-9)";
    if (!/[!@#$%^&*()\-_=+[\]{}|;:,.<>?~`]/.test(pass)) {
      errors.special = "En az bir özel karakter içermeli: !@#$%^&*()-_=+[]{}|;:,.<>?~`";
    }
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const passwordErrors = validatePassword(password);
    const newErrors = { ...passwordErrors };
    
    if (password !== confirmPassword) {
      newErrors.confirm = "Şifreler eşleşmiyor";
    }
    
    if (Object.keys(newErrors).length === 0) {
      setIsLoading(true);
      try {
        await apiService.resetPassword(email, code, password);
        onNext({ password });
      } catch (error) {
        console.error('Password reset failed:', error);
        if (error.message.includes('400')) {
          setErrors({ general: "Geçersiz veri sağlandı. Lütfen tekrar deneyin." });
        } else {
          setErrors({ general: "Şifre sıfırlama başarısız. Lütfen tekrar deneyin." });
        }
      } finally {
        setIsLoading(false);
      }
    } else {
      setErrors(newErrors);
    }
  };

  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Şifrenizi sıfırlayın</h3>
      <h2 className="form-title">Lütfen hatırlayacağınız bir şey yazın</h2>

      {errors.general && (
        <div style={{ 
          color: '#ef4444', 
          fontSize: '14px', 
          marginBottom: '16px',
          padding: '12px',
          background: '#fef2f2',
          borderRadius: '8px',
          border: '1px solid #fecaca'
        }}>
          {errors.general}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="newPassword">Yeni şifre</label>
          <div className="password-wrapper">
            <input
              type={showPassword ? "text" : "password"}
              id="newPassword"
              name="newPassword"
              placeholder="en az 8 karakter olmalı"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
                              className={errors.length || errors.uppercase || errors.lowercase || errors.digit || errors.special ? "error" : ""}
              disabled={isLoading}
            />
            <img
              src={showPassword ? "/images/close-eye.png" : "/images/seen.png"}
              alt={showPassword ? "Şifreyi gizle" : "Şifreyi göster"}
              className="eye-icon"
              onClick={() => setShowPassword(!showPassword)}
              role="button"
              tabIndex={0}
            />
          </div>
          {(errors.length || errors.uppercase || errors.lowercase || errors.digit || errors.special) && (
            <div style={{ fontSize: 12, color: "#ef4444", marginTop: 4 }}>
              {errors.length && <div>{errors.length}</div>}
              {errors.uppercase && <div>{errors.uppercase}</div>}
              {errors.lowercase && <div>{errors.lowercase}</div>}
              {errors.digit && <div>{errors.digit}</div>}
              {errors.special && <div>{errors.special}</div>}
            </div>
          )}
        </div>
        <div className="form-group">
          <label htmlFor="confirmPassword">Yeni şifreyi onayla</label>
          <div className="password-wrapper">
            <input
              type={showConfirmPassword ? "text" : "password"}
              id="confirmPassword"
              name="confirmPassword"
              placeholder="şifreyi tekrarla"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className={errors.confirm ? "error" : ""}
              disabled={isLoading}
            />
            <img
              src={showConfirmPassword ? "/images/close-eye.png" : "/images/seen.png"}
              alt={showConfirmPassword ? "Şifreyi gizle" : "Şifreyi göster"}
              className="eye-icon"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              role="button"
              tabIndex={0}
            />
          </div>
          {errors.confirm && <div style={{ fontSize: 12, color: "#ef4444", marginTop: 4 }}>{errors.confirm}</div>}
        </div>
        <button 
          type="submit" 
          className="login-button"
          disabled={isLoading}
          style={{ 
            opacity: isLoading ? 0.7 : 1,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: "8px"
          }}
        >
          {isLoading && (
            <div style={{
              width: "16px",
              height: "16px",
              border: "2px solid rgba(255,255,255,0.3)",
              borderTop: "2px solid #fff",
              borderRadius: "50%",
              animation: "spin 1s linear infinite"
            }}></div>
          )}
          {isLoading ? "Sıfırlanıyor..." : "Şifreyi Sıfırla"}
        </button>
      </form>
      <div className="signup-prompt">
        Zaten hesabınız var mı? <a href="/login">Giriş yapın</a>
      </div>
    </div>
  );
}

// Adım 4: Başarı
function SuccessStep() {
  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Şifre değiştirildi</h3>
      <h2 className="form-title">Şifreniz başarıyla değiştirildi</h2>
      <button
        onClick={() => (window.location.href = "/login")}
        className="login-button"
        style={{ marginTop: 32 }}
      >
        Giriş sayfasına dön
      </button>
    </div>
  );
}

export default function ForgotPassword() {
  const [step, setStep] = useState(1);
  const [data, setData] = useState({});

  const handleNext = (stepData) => {
    setData({ ...data, ...stepData });
    setStep(step + 1);
  };

  const handleBack = () => {
    setStep(step - 1);
  };

  const renderStep = () => {
    switch (step) {
      case 1:
        return <EmailStep onNext={handleNext} onBack={handleBack} />;
      case 2:
        return <CodeStep onNext={handleNext} onBack={handleBack} email={data.email} />;
      case 3:
        return <ResetPasswordStep onNext={handleNext} onBack={handleBack} email={data.email} code={data.code} />;
      case 4:
        return <SuccessStep />;
      default:
        return <EmailStep onNext={handleNext} onBack={handleBack} />;
    }
  };

  return (
    <div 
      className="login-container"
      style={{
        backgroundImage: "url('/images/background-gradient.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        minHeight: "100vh",
        width: "100vw"
      }}
    >
      <div className="left-panel">
        <LeftPanel />
      </div>

      <div className="right-panel">
        {renderStep()}
      </div>
      
      <style>{`
        @keyframes spin {
          0% { 
            transform: rotate(0deg);
            -webkit-transform: rotate(0deg);
            -moz-transform: rotate(0deg);
            -ms-transform: rotate(0deg);
          }
          100% { 
            transform: rotate(360deg);
            -webkit-transform: rotate(360deg);
            -moz-transform: rotate(360deg);
            -ms-transform: rotate(360deg);
          }
        }
        
        @-webkit-keyframes spin {
          0% { -webkit-transform: rotate(0deg); }
          100% { -webkit-transform: rotate(360deg); }
        }
        
        @-moz-keyframes spin {
          0% { -moz-transform: rotate(0deg); }
          100% { -moz-transform: rotate(360deg); }
        }
        
        @-ms-keyframes spin {
          0% { -ms-transform: rotate(0deg); }
          100% { -ms-transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
} 