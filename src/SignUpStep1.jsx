import { useState } from 'react';
import './LoginPage.css';
import { StepIndicator } from './components/StepIndicator';

export function SignUpStep1({ onNext, onBack, userData, passwordData }) {
  const [firstName, setFirstName] = useState(userData?.firstName || '');
  const [lastName, setLastName] = useState(userData?.lastName || '');
  const [phone, setPhone] = useState(userData?.phone || '');
  const [email, setEmail] = useState(userData?.email || '');
  const [errors, setErrors] = useState({});

  const validateForm = () => {
    const newErrors = {};

    // İsim validasyonu
    if (!firstName) {
      newErrors.firstName = 'Ad gerekli';
    } else if (!/^[a-zA-ZğüşıöçĞÜŞİÖÇ]{2,}$/.test(firstName)) {
      newErrors.firstName = 'Ad en az 2 karakter olmalı ve yalnızca harf içermeli';
    }

    // Soyisim validasyonu
    if (!lastName) {
      newErrors.lastName = 'Soyad gerekli';
    } else if (!/^[a-zA-ZğüşıöçĞÜŞİÖÇ]{2,}$/.test(lastName)) {
      newErrors.lastName = 'Soyad en az 2 karakter olmalı ve yalnızca harf içermeli';
    }

    // Telefon validasyonu
    if (!phone) {
      newErrors.phone = 'Telefon numarası gerekli';
    } else if (!/^\d{10}$/.test(phone)) {
      newErrors.phone = 'Telefon numarası 10 haneli olmalı';
    } else if (phone.startsWith('0')) {
      newErrors.phone = 'Numaranızı başında sıfır olmadan girin';
    }

    // Email validasyonu
    if (!email) {
      newErrors.email = 'E-posta gerekli';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = 'Geçerli bir e-posta adresi girin';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      // Form verilerini onNext'e geçir (şifre verileri varsa onları da koru)
      onNext({
        firstName,
        lastName,
        phone,
        email,
        ...(passwordData && { passwordData }) // Şifre verileri varsa dahil et
      });
    }
  };

  return (
    <div 
      className="login-form-container" 
      style={{ 
        minWidth: '400px',
        maxWidth: '100%'
      }}
    >
      <StepIndicator currentStep={1} />
      <h2 className="form-title">Hesabınızı Oluşturun</h2>

      <div className="form-fields-flex">
        <div className="form-group icon-input">
          <label htmlFor="firstName">Ad</label>
          <div className="input-with-icon">
            <span className="input-icon">👤</span>
            <input 
              type="text" 
              id="firstName" 
              name="firstName" 
              placeholder="Adınızı girin" 
              value={firstName} 
              onChange={(e) => setFirstName(e.target.value)}
              className={errors.firstName ? "error" : ""}
            />
          </div>
          {errors.firstName && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.firstName}</div>}
        </div>

        <div className="form-group icon-input">
          <label htmlFor="lastName">Soyad</label>
          <div className="input-with-icon">
            <span className="input-icon">👤</span>
            <input 
              type="text" 
              id="lastName" 
              name="lastName" 
              placeholder="Soyadınızı girin" 
              value={lastName} 
              onChange={(e) => setLastName(e.target.value)}
              className={errors.lastName ? "error" : ""}
            />
          </div>
          {errors.lastName && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.lastName}</div>}
        </div>

        <div className="form-group icon-input">
          <label htmlFor="phone">Telefon Numarası</label>
          <div className="input-with-icon">
            <span className="input-icon">📞</span>
            <input 
              type="tel" 
              id="phone" 
              name="phone" 
              placeholder="Telefon numaranızı girin" 
              value={phone}
              maxLength={10}
              inputMode="numeric"
              pattern="[0-9]*"
              onChange={(e) => {
                const inputValue = e.target.value;
                // Sadece rakamları kabul et
                const numericValue = inputValue.replace(/[^0-9]/g, '');
                setPhone(numericValue);
              }}
              className={errors.phone ? "error" : ""}
            />
          </div>
          {errors.phone && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.phone}</div>}
        </div>

        <div className="form-group icon-input">
          <label htmlFor="email">E-posta</label>
          <div className="input-with-icon">
            <span className="input-icon">📧</span>
            <input 
              type="text" 
              id="email" 
              name="email" 
              placeholder="E-posta adresinizi girin" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)}
              className={errors.email ? "error" : ""}
            />
          </div>
          {errors.email && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.email}</div>}
        </div>
      </div>

      <button
        type="submit"
        className="login-button"
        onClick={handleSubmit}
      >
        Sonraki Adım
      </button>

      <div className="signup-prompt">
        Zaten hesabınız var mı?{' '}
        <a
          href="#"
          onClick={(e) => {
            e.preventDefault();
            onBack();
          }}
        >
          Giriş yapın
        </a>
      </div>
    </div>
  );
}