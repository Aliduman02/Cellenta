import { useState } from 'react';
import apiService from './services/api';
import './LoginPage.css';

export function SignUpStep2({ onBack, userData }) {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const validatePassword = (pass) => {
    const errors = {};
    if (pass.length < 8) errors.length = "Must be at least 8 characters";
    if (!/[A-Z]/.test(pass)) errors.uppercase = "Must contain at least 1 uppercase letter";
    if (!/[a-z]/.test(pass)) errors.lowercase = "Must contain at least 1 lowercase letter";
    if (!/[0-9]/.test(pass)) errors.digit = "Must contain at least 1 digit";
    if (!/[!@#$%^&*()\-_=+]/.test(pass)) errors.special = "Must contain at least 1 special character (!@#$%^&*()-_+=)";
    return errors;
  };

  const validateForm = () => {
    const newErrors = {};
    
    // Şifre validasyonu
    if (!password) {
      newErrors.password = "Password is required";
    } else {
      const passwordErrors = validatePassword(password);
      Object.assign(newErrors, passwordErrors);
    }

    // Şifre onayı
    if (!confirmPassword) {
      newErrors.confirmPassword = "Password confirmation is required";
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    try {
      // API'ye gönderilecek veri
      const signupData = {
        phone: userData.phone,
        password,
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email
      };

      const response = await apiService.signup(signupData);
      
      // Dashboard'a yönlendir
      window.location.href = '/dashboard';
      
    } catch (error) {
      console.error('Signup failed:', error);
      
      // API'den gelen hata mesajlarını göster
      if (error.message.includes('409') || error.message.includes('already exists')) {
        setErrors({ general: "User already exists with this phone number or email" });
      } else if (error.message.includes('400') || error.message.includes('Invalid data')) {
        setErrors({ general: "Invalid data provided. Please check your information." });
      } else {
        setErrors({ general: error.message || "Signup failed. Please try again." });
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-form-container" style={{ minWidth: '400px' }}>
      <h3 className="form-subtitle">Step 2/2</h3>
      <h2 className="form-title">Set Your Password</h2>

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
          <label htmlFor="password">Create Password</label>
          <div className="input-with-icon">
            <span className="input-icon">🔒</span>
            <input
              type={showPassword ? "text" : "password"}
              id="password"
              name="password"
              placeholder="Must be 8+ characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={errors.password || errors.length || errors.uppercase || errors.special ? "error" : ""}
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
          {(errors.password || errors.length || errors.uppercase || errors.special) && (
            <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>
              {errors.password && <div>{errors.password}</div>}
              {errors.length && <div>{errors.length}</div>}
              {errors.uppercase && <div>{errors.uppercase}</div>}
              {errors.special && <div>{errors.special}</div>}
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="confirmPassword">Confirm Password</label>
          <div className="input-with-icon">
            <span className="input-icon">🔒</span>
            <input
              type={showConfirmPassword ? "text" : "password"}
              id="confirmPassword"
              name="confirmPassword"
              placeholder="Repeat your password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className={errors.confirmPassword ? "error" : ""}
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
          {errors.confirmPassword && <div style={{ color: '#ef4444', fontSize: '13px', marginTop: '4px' }}>{errors.confirmPassword}</div>}
        </div>
      </div>

      <button
        type="submit"
        className="login-button"
        onClick={handleSubmit}
        disabled={isLoading}
        style={{ opacity: isLoading ? 0.7 : 1 }}
      >
        {isLoading ? "Creating account..." : "Sign up"}
      </button>
    </div>
  );
}
  