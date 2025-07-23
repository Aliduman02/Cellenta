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
      newErrors.phone = 'Telefon numarası gerekli';
    } else if (!/^\d{10}$/.test(phone)) {
      newErrors.phone = 'Telefon numarası 10 haneli olmalı';
    } else if (phone.startsWith('0')) {
      newErrors.phone = 'Numaranızı başında sıfır olmadan girin';
    }

    // Şifre validasyonu
    if (!password) {
      newErrors.password = 'Şifre gerekli';
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
        setErrors({ general: 'Geçersiz telefon numarası veya şifre' });
      } else if (error.message.includes('404') || error.message.includes('User not found')) {
        setErrors({ general: 'Hesap bulunamadı. Lütfen önce kayıt olun.' });
      } else {
        setErrors({ general: error.message || 'Giriş başarısız. Lütfen tekrar deneyin.' });
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-form-container">
      <h3 className="form-subtitle">Tekrar Hoş Geldiniz</h3>
      <h2 className="form-title">Hesabınıza Giriş Yapın</h2>

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
        <div className="form-fields-flex">
        <div className="form-group">
          <label htmlFor="phone">Telefon Numarası</label>
          <input
            type="tel"
            id="phone"
            name="phone"
            placeholder="Telefon numaranızı girin"
            value={phone}
            maxLength={10}
            inputMode="numeric"
            pattern="[0-9]*"
            onChange={e => {
              const inputValue = e.target.value;
              // Sadece rakamları kabul et
              const numericValue = inputValue.replace(/[^0-9]/g, '');
              setPhone(numericValue);
              
              // Remember Me aktifse telefonu güncelle
              if (rememberMe && numericValue) {
                localStorage.setItem('rememberedPhone', numericValue);
              }
            }}
            className={errors.phone ? "error" : ""}
            disabled={isLoading}
          />
          {errors.phone && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.phone}</div>}
        </div>

        <div className="form-group password-group">
          <label htmlFor="password">Şifre</label>
          <div className="password-wrapper">
            <input
              type={showPassword ? 'text' : 'password'}
              id="password"
              name="password"
              placeholder="Şifrenizi girin"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className={errors.password ? "error" : ""}
              disabled={isLoading}
            />
            <img
              src={showPassword ? "/images/close-eye.png" : "/images/seen.png"}
              alt={showPassword ? "Şifreyi gizle" : "Şifreyi göster"}
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
          <span className="remember-label">Beni Hatırla</span>
        </label>

        <a className="forgot-password" href="#" onClick={(e) => { e.preventDefault(); window.location.href = "/forgot-password"; }}>Şifremi Unuttum</a>
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
          {isLoading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
        </button>
      </form>

      {/* Debug: API Test Button */}
      {/* Bu buton kaldırıldı */}

      <div className="signup-prompt">
        Hesabınız yok mu?{' '}
        <a href="#" onClick={(e) => { e.preventDefault(); onSignUpClick(); }}>
          Hesap oluşturun
        </a>
      </div>
      
      <style jsx>{`
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
