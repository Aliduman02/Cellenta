import React, { useState, useEffect } from "react";
import apiService from "./services/api";
import { LeftPanel } from "./LeftPanel";
import "./LoginPage.css";

// Adım 1: Email girişi
function EmailStep({ onNext, onBack }) {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email) {
      setError("Email is required");
      return;
    }

    setIsLoading(true);
    try {
      await apiService.forgotPassword(email);
      onNext({ email });
    } catch (error) {
      console.error('Forgot password failed:', error);
      if (error.message.includes('404')) {
        setError("No such email is registered");
      } else {
        setError("Failed to send code. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Forgot password?</h3>
      <h2 className="form-title">Don't worry! It happens.</h2>
      <p style={{ fontSize: 16, color: "#666", marginBottom: 32, textAlign: "center" }}>
        Please enter the email associated with your account.
      </p>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
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
          style={{ opacity: isLoading ? 0.7 : 1 }}
        >
          {isLoading ? "Sending..." : "Send code"}
        </button>
      </form>
      <div className="signup-prompt">
        Remember password? <a href="/login">Log in</a>
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
      setError("Please enter 6-digit code");
      return;
    }

    setIsLoading(true);
    try {
      await apiService.verifyCode(email, fullCode);
      onNext({ code: fullCode });
    } catch (error) {
      console.error('Code verification failed:', error);
      if (error.message.includes('400')) {
        setError("Invalid code");
      } else {
        setError("Verification failed. Please try again.");
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
      setError("Failed to resend code. Please try again.");
    }
  };

  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Please, check your mail</h3>
      <h2 className="form-title">We've sent a code to your mail</h2>
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
          style={{ opacity: isLoading ? 0.7 : 1 }}
        >
          {isLoading ? "Verifying..." : "Verify"}
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
            Send code again
          </button>
        ) : (
          <span>Send code again {Math.floor(timeLeft / 60)}:{(timeLeft % 60).toString().padStart(2, '0')}</span>
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
    if (pass.length < 8) errors.length = "Must be at least 8 characters";
    if (!/[A-Z]/.test(pass)) errors.uppercase = "Must contain at least 1 uppercase letter";
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(pass)) errors.special = "Must contain at least 1 special character";
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const passwordErrors = validatePassword(password);
    const newErrors = { ...passwordErrors };
    
    if (password !== confirmPassword) {
      newErrors.confirm = "Passwords do not match";
    }
    
    if (Object.keys(newErrors).length === 0) {
      setIsLoading(true);
      try {
        await apiService.resetPassword(email, code, password);
        onNext({ password });
      } catch (error) {
        console.error('Password reset failed:', error);
        if (error.message.includes('400')) {
          setErrors({ general: "Invalid data provided. Please try again." });
        } else {
          setErrors({ general: "Password reset failed. Please try again." });
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
      <h3 className="form-subtitle">Reset your password</h3>
      <h2 className="form-title">Please type something you'll remember</h2>

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
          <label htmlFor="newPassword">New password</label>
          <div className="password-wrapper">
            <input
              type={showPassword ? "text" : "password"}
              id="newPassword"
              name="newPassword"
              placeholder="must be 8 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={errors.length || errors.uppercase || errors.special ? "error" : ""}
              disabled={isLoading}
            />
            <img
              src={showPassword ? "/images/close-eye.png" : "/images/seen.png"}
              alt={showPassword ? "Hide password" : "Show password"}
              className="eye-icon"
              onClick={() => setShowPassword(!showPassword)}
              role="button"
              tabIndex={0}
            />
          </div>
          {(errors.length || errors.uppercase || errors.special) && (
            <div style={{ fontSize: 12, color: "#ef4444", marginTop: 4 }}>
              {errors.length && <div>{errors.length}</div>}
              {errors.uppercase && <div>{errors.uppercase}</div>}
              {errors.special && <div>{errors.special}</div>}
            </div>
          )}
        </div>
        <div className="form-group">
          <label htmlFor="confirmPassword">Confirm new password</label>
          <div className="password-wrapper">
            <input
              type={showConfirmPassword ? "text" : "password"}
              id="confirmPassword"
              name="confirmPassword"
              placeholder="repeat password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className={errors.confirm ? "error" : ""}
              disabled={isLoading}
            />
            <img
              src={showConfirmPassword ? "/images/close-eye.png" : "/images/seen.png"}
              alt={showConfirmPassword ? "Hide password" : "Show password"}
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
          style={{ opacity: isLoading ? 0.7 : 1 }}
        >
          {isLoading ? "Resetting..." : "Reset password"}
        </button>
      </form>
      <div className="signup-prompt">
        Already have an account? <a href="/login">Log in</a>
      </div>
    </div>
  );
}

// Adım 4: Başarı
function SuccessStep() {
  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Password changed</h3>
      <h2 className="form-title">Your password has been changed successfully</h2>
      <button
        onClick={() => (window.location.href = "/login")}
        className="login-button"
        style={{ marginTop: 32 }}
      >
        Back to login
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
    </div>
  );
} 