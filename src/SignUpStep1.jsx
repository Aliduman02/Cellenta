import { useState } from 'react';
import './LoginPage.css';

export function SignUpStep1({ onNext, onBack }) {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [errors, setErrors] = useState({});

  const validateForm = () => {
    const newErrors = {};

    // İsim validasyonu
    if (!firstName) {
      newErrors.firstName = "First name is required";
    } else if (!/^[a-zA-ZğüşıöçĞÜŞİÖÇ]{2,}$/.test(firstName)) {
      newErrors.firstName = "First name must be at least 2 characters";
    }

    // Soyisim validasyonu
    if (!lastName) {
      newErrors.lastName = "Last name is required";
    } else if (!/^[a-zA-ZğüşıöçĞÜŞİÖÇ]{2,}$/.test(lastName)) {
      newErrors.lastName = "Last name must be at least 2 characters";
    }

    // Telefon validasyonu
    if (!phone) {
      newErrors.phone = "Phone number is required";
    } else if (!/^\d{10}$/.test(phone)) {
      newErrors.phone = "Phone number must be 10 digits";
    } else if (phone.startsWith('0')) {
      newErrors.phone = "Enter your number without leading zero";
    }

    // Email validasyonu
    if (!email) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = "Please enter a valid email address";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validateForm()) {
      // Form verilerini onNext'e geçir
      onNext({
        firstName,
        lastName,
        phone,
        email
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
      <h3 className="form-subtitle">Step 1 of 2</h3>
      <h2 className="form-title">Create Your Account</h2>

      <div className="form-fields-flex">
        <div className="form-group icon-input">
          <label htmlFor="firstName">First Name</label>
          <div className="input-with-icon">
            <span className="input-icon">👤</span>
            <input 
              type="text" 
              id="firstName" 
              name="firstName" 
              placeholder="Enter your first name" 
              value={firstName} 
              onChange={(e) => setFirstName(e.target.value)}
              className={errors.firstName ? "error" : ""}
            />
          </div>
          {errors.firstName && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.firstName}</div>}
        </div>

        <div className="form-group icon-input">
          <label htmlFor="lastName">Last Name</label>
          <div className="input-with-icon">
            <span className="input-icon">👤</span>
            <input 
              type="text" 
              id="lastName" 
              name="lastName" 
              placeholder="Enter your last name" 
              value={lastName} 
              onChange={(e) => setLastName(e.target.value)}
              className={errors.lastName ? "error" : ""}
            />
          </div>
          {errors.lastName && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.lastName}</div>}
        </div>

        <div className="form-group icon-input">
          <label htmlFor="phone">Phone Number</label>
          <div className="input-with-icon">
            <span className="input-icon">📞</span>
            <input 
              type="text" 
              id="phone" 
              name="phone" 
              placeholder="Enter your phone number" 
              value={phone} 
              onChange={(e) => setPhone(e.target.value)}
              className={errors.phone ? "error" : ""}
            />
          </div>
          {errors.phone && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.phone}</div>}
        </div>

        <div className="form-group icon-input">
          <label htmlFor="email">Email Address</label>
          <div className="input-with-icon">
            <span className="input-icon">📧</span>
            <input 
              type="text" 
              id="email" 
              name="email" 
              placeholder="Enter your email" 
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
        Next Step
      </button>

      <div className="signup-prompt">
        Already have an account?{' '}
        <a
          href="#"
          onClick={(e) => {
            e.preventDefault();
            onBack();
          }}
        >
          Log In
        </a>
      </div>
    </div>
  );
}