import React from 'react';
import apiService from './services/api';

export function LoginForm({ onSignUpClick }) {
  const [showPassword, setShowPassword] = React.useState(false);
  const [rememberMe, setRememberMe] = React.useState(false);
  const [phone, setPhone] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [errors, setErrors] = React.useState({});
  const [isLoading, setIsLoading] = React.useState(false);

  // Sayfa yüklendiğinde Remember Me verilerini kontrol et
  React.useEffect(() => {
    const savedRememberMe = localStorage.getItem('rememberMe') === 'true';
    const savedPhone = localStorage.getItem('rememberedPhone');
    
    if (savedRememberMe && savedPhone) {
      setRememberMe(true);
      setPhone(savedPhone);
      console.log('Remembered phone loaded:', savedPhone);
    }
  }, []);

  // Remember Me değiştiğinde localStorage'ı güncelle
  const handleRememberMeChange = (checked) => {
    setRememberMe(checked);
    
    if (checked) {
      // Remember Me aktif - telefonu kaydet (eğer var ise)
      if (phone) {
        localStorage.setItem('rememberMe', 'true');
        localStorage.setItem('rememberedPhone', phone);
        console.log('Phone saved for remember me:', phone);
      }
    } else {
      // Remember Me kapalı - kayıtlı verileri temizle
      localStorage.removeItem('rememberMe');
      localStorage.removeItem('rememberedPhone');
      console.log('Remember me data cleared');
    }
  };

  const validateForm = () => {
    const newErrors = {};

    // Telefon validasyonu
    if (!phone) {
      newErrors.phone = "Phone number is required";
    } else if (!/^\d{10}$/.test(phone)) {
      newErrors.phone = "Phone number must be 10 digits";
    } else if (phone.startsWith('0')) {
      newErrors.phone = "Enter your number without leading zero";
    }

    // Şifre validasyonu
    if (!password) {
      newErrors.password = "Password is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    try {
      const response = await apiService.login(phone, password);
      
      // Remember me seçeneği - başarılı login sonrası telefonu kaydet
      if (rememberMe) {
        localStorage.setItem('rememberMe', 'true');
        localStorage.setItem('rememberedPhone', phone);
        console.log('Login successful - phone saved for remember me:', phone);
      } else {
        // Remember me kapalıysa kayıtlı verileri temizle
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('rememberedPhone');
      }

      // Dashboard'a yönlendir
      window.location.href = '/dashboard';
      
    } catch (error) {
      console.error('Login failed:', error);
      
      // API'den gelen hata mesajlarını göster
      if (error.message.includes('401') || error.message.includes('Invalid credentials')) {
        setErrors({ general: "Invalid phone number or password" });
      } else if (error.message.includes('404') || error.message.includes('User not found')) {
        setErrors({ general: "Account not found. Please sign up first." });
      } else {
        setErrors({ general: error.message || "Login failed. Please try again." });
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Welcome back</h3>
      <h2 className="form-title">Log In to your Account</h2>

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

      <div className="form-fields-flex">
        <div className="form-group">
          <label htmlFor="phone">Phone Number</label>
          <input
            type="tel"
            id="phone"
            name="phone"
            placeholder="Enter your phone number"
            value={phone}
            onChange={e => {
              const newPhone = e.target.value;
              setPhone(newPhone);
              
              // Remember Me aktifse telefonu güncelle
              if (rememberMe && newPhone) {
                localStorage.setItem('rememberedPhone', newPhone);
              }
            }}
            className={errors.phone ? "error" : ""}
            disabled={isLoading}
          />
          {errors.phone && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.phone}</div>}
        </div>

        <div className="form-group password-group">
          <label htmlFor="password">Password</label>
          <div className="password-wrapper">
            <input
              type={showPassword ? 'text' : 'password'}
              id="password"
              name="password"
              placeholder="Enter your password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className={errors.password ? "error" : ""}
              disabled={isLoading}
            />
            <img
              src={showPassword ? "/images/close-eye.png" : "/images/seen.png"}
              alt={showPassword ? "Hide password" : "Show password"}
              className="eye-icon"
              onClick={() => setShowPassword(prev => !prev)}
              role="button"
              tabIndex={0}
            />
          </div>
          {errors.password && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.password}</div>}
        </div>
      </div>

      <div className="form-options-row">
        <label className="switch remember-switch">
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={() => handleRememberMeChange(!rememberMe)}
            disabled={isLoading}
          />
          <span className="slider" />
          <span className="remember-label">Remember Me</span>
        </label>

        <a className="forgot-password" href="#" onClick={(e) => { e.preventDefault(); window.location.href = "/forgot-password"; }}>Forgot Password?</a>
      </div>

      <button 
        type="submit" 
        className="login-button" 
        onClick={handleSubmit}
        disabled={isLoading}
        style={{ opacity: isLoading ? 0.7 : 1 }}
      >
        {isLoading ? "Logging in..." : "Log In"}
      </button>

      {/* Debug: API Test Button */}
      {/* Bu buton kaldırıldı */}

      <div className="signup-prompt">
        Don't have an account?{' '}
        <a href="#" onClick={(e) => { e.preventDefault(); onSignUpClick(); }}>
          Create an account
        </a>
      </div>
    </div>
  );
}
