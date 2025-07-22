import { useState, useEffect } from 'react';
import apiService from './services/api';
import './LoginPage.css';

export function SignUpStep2({ onBack, userData }) {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [packages, setPackages] = useState([]);
  const [selectedPackage, setSelectedPackage] = useState(null);
  const [packagesLoading, setPackagesLoading] = useState(true);
  const [isPackageDropdownOpen, setIsPackageDropdownOpen] = useState(false);

  // Paketleri yükle
  useEffect(() => {
    const loadPackages = async () => {
      try {
        setPackagesLoading(true);
        const packagesData = await apiService.getPackages();
        
        const formattedPackages = packagesData.map(pkg => ({
          id: pkg.package_id,
          name: pkg.packageName,
          price: pkg.price,
          amountMinutes: pkg.amountMinutes,
          amountData: pkg.amountData,
          amountSms: pkg.amountSms,
          period: pkg.period,
          summary: `${pkg.amountMinutes} dk, ${pkg.amountData} MB, ${pkg.amountSms} SMS - ${pkg.period} gün`
        }));
        
        setPackages(formattedPackages);
      } catch (error) {
        console.error('Failed to load packages:', error);
        setErrors({ general: 'Failed to load packages. Please refresh the page.' });
      } finally {
        setPackagesLoading(false);
      }
    };

    loadPackages();
  }, []);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (isPackageDropdownOpen && !event.target.closest('.package-dropdown')) {
        setIsPackageDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isPackageDropdownOpen]);

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

    // Paket seçimi validasyonu
    if (!selectedPackage) {
      newErrors.package = "Please select a package to continue";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    try {
      // İlk önce kullanıcıyı kaydet
      const signupData = {
        phone: userData.phone,
        password,
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email
      };

      const response = await apiService.signup(signupData);
      console.log('Signup successful:', response);
      
      // Kayıt başarılıysa paketi satın al
      try {
        console.log('Attempting to purchase package:', selectedPackage);
        
        // Package purchase API'sini dene
        const purchaseResult = await apiService.purchasePackage(selectedPackage.id);
        console.log('Package purchased successfully:', purchaseResult);
        
        // Package işleminin API'de tamamlanması için kısa bir bekleme
        await new Promise(resolve => setTimeout(resolve, 2000));
        
      } catch (packageError) {
        console.warn('Package purchase failed, trying assignPackageToCustomer:', packageError);
        
        // Alternatif olarak assignPackageToCustomer deneyelim
                 try {
           const assignResult = await apiService.assignPackageToCustomer(response.cust_id, selectedPackage.id);
           console.log('Package assigned successfully:', assignResult);
           
           // Package assign işleminin tamamlanması için bekleme
           await new Promise(resolve => setTimeout(resolve, 2000));
           
         } catch (assignError) {
           console.error('Both package purchase methods failed:', assignError);
           // Kullanıcıya bilgi verebiliriz ama kayıt başarılı olduğu için devam ediyoruz
           setErrors({ 
             general: 'Account created successfully, but package assignment failed. You can select a package from the Store page later.' 
           });
         }
      }
      
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
    <div 
      className="login-form-container" 
      style={{ 
        minWidth: '480px',
        maxWidth: '100%',
        overflow: 'visible'
      }}
    >
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

      {/* Package Selection Dropdown */}
      <div className="package-dropdown" style={{ 
        marginTop: '24px', 
        marginBottom: '24px',
        position: 'relative' 
      }}>
        <label style={{ 
          display: 'block', 
          fontSize: '14px', 
          fontWeight: 600, 
          marginBottom: '12px', 
          color: '#333' 
        }}>
          📦 Choose Your Package
        </label>
        
        {/* Package Dropdown Button */}
        <div
          onClick={() => setIsPackageDropdownOpen(!isPackageDropdownOpen)}
          style={{
            padding: '16px 20px',
            border: '1px solid #d1d5db',
            borderRadius: '8px',
            background: '#fff',
            cursor: 'pointer',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            transition: 'all 0.3s ease',
            boxShadow: isPackageDropdownOpen ? '0 0 0 2px rgba(59, 130, 246, 0.15)' : '0 1px 3px rgba(0,0,0,0.05)',
            borderColor: isPackageDropdownOpen ? '#60a5fa' : '#d1d5db',
            zIndex: 10,
            position: 'relative'
          }}
          onMouseEnter={(e) => {
            if (!isPackageDropdownOpen) {
              e.currentTarget.style.borderColor = '#9ca3af';
            }
          }}
          onMouseLeave={(e) => {
            if (!isPackageDropdownOpen) {
              e.currentTarget.style.borderColor = '#d1d5db';
            }
          }}
        >
          <div style={{ flex: 1 }}>
            {packagesLoading ? (
              <div style={{ 
                fontSize: '14px', 
                color: '#6b7280',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <div style={{
                  width: '16px',
                  height: '16px',
                  border: '2px solid #e5e7eb',
                  borderTop: '2px solid #6366f1',
                  borderRadius: '50%',
                  animation: 'spin 1s linear infinite'
                }}></div>
                Loading packages...
              </div>
            ) : selectedPackage ? (
              <div>
                <div style={{ 
                  fontWeight: 600, 
                  fontSize: '15px', 
                  color: '#1f2937',
                  marginBottom: '2px'
                }}>
                  {selectedPackage.name}
                </div>
                <div style={{ 
                  fontSize: '13px', 
                  color: '#6b7280'
                }}>
                  {selectedPackage.summary}
                </div>
              </div>
            ) : (
              <div style={{ 
                fontSize: '15px', 
                color: '#9ca3af' 
              }}>
                Select a package...
              </div>
            )}
          </div>
          
          {selectedPackage && (
            <div style={{ 
              fontSize: '16px', 
              fontWeight: 700, 
              color: '#059669',
              marginRight: '12px'
            }}>
              {selectedPackage.price} TL
            </div>
          )}
          
          <div style={{ 
            fontSize: '16px', 
            color: isPackageDropdownOpen ? '#60a5fa' : '#9ca3af',
            transform: isPackageDropdownOpen ? 'rotate(180deg) scale(0.9)' : 'rotate(0deg) scale(1)',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            width: '20px',
            height: '20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            ▼
          </div>
        </div>

        {/* Package Dropdown Content - Apple Style Overlay */}
        {isPackageDropdownOpen && !packagesLoading && (
          <div style={{ 
            position: 'absolute',
            top: '100%',
            left: '0',
            right: '0',
            marginTop: '4px',
            border: '1px solid #e5e7eb',
            borderRadius: '12px',
            background: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 20px 40px rgba(0,0,0,0.15), 0 8px 24px rgba(0,0,0,0.1)',
            maxHeight: '280px',
            overflowY: 'auto',
            zIndex: 1000,
            animation: 'appleSlideDown 0.35s cubic-bezier(0.4, 0, 0.2, 1)'
          }}>
            {packages.map((pkg, index) => (
                              <div
                key={pkg.id}
                onClick={() => {
                  setSelectedPackage(pkg);
                  setIsPackageDropdownOpen(false);
                }}
                style={{
                  padding: '14px 20px',
                  cursor: 'pointer',
                  borderBottom: index < packages.length - 1 ? '1px solid rgba(0,0,0,0.06)' : 'none',
                  background: selectedPackage?.id === pkg.id ? 'rgba(59, 130, 246, 0.08)' : 'transparent',
                  transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  borderRadius: index === 0 ? '12px 12px 0 0' : (index === packages.length - 1 ? '0 0 12px 12px' : '0'),
                  margin: '0 -1px'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = selectedPackage?.id === pkg.id 
                    ? 'rgba(59, 130, 246, 0.12)' 
                    : 'rgba(0, 0, 0, 0.04)';
                  e.currentTarget.style.transform = 'scale(0.998)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = selectedPackage?.id === pkg.id 
                    ? 'rgba(59, 130, 246, 0.08)' 
                    : 'transparent';
                  e.currentTarget.style.transform = 'scale(1)';
                }}
              >
                <div style={{ flex: 1 }}>
                  <div style={{ 
                    fontWeight: 600, 
                    fontSize: '15px', 
                    color: '#1f2937',
                    marginBottom: '4px'
                  }}>
                    {pkg.name}
                  </div>
                  <div style={{ 
                    fontSize: '13px', 
                    color: '#6b7280'
                  }}>
                    {pkg.summary}
                  </div>
                </div>
                <div style={{ 
                  fontSize: '16px', 
                  fontWeight: 700, 
                  color: '#059669',
                  marginLeft: '16px'
                }}>
                  {pkg.price} TL
                </div>
                {selectedPackage?.id === pkg.id && (
                  <div style={{ 
                    marginLeft: '12px',
                    color: '#3b82f6',
                    fontSize: '16px',
                    fontWeight: 700,
                    width: '20px',
                    height: '20px',
                    borderRadius: '50%',
                    background: 'rgba(59, 130, 246, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    animation: 'checkmarkBounce 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55)'
                  }}>
                    ✓
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
        
        {errors.package && (
          <div style={{ 
            color: '#ef4444', 
            fontSize: '13px', 
            marginTop: '8px' 
          }}>
            {errors.package}
          </div>
        )}
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

      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        
        @keyframes appleSlideDown {
          0% { 
            opacity: 0; 
            transform: translateY(-8px) scale(0.98);
            filter: blur(8px);
          }
          40% {
            opacity: 0.6;
            transform: translateY(-2px) scale(0.995);
            filter: blur(2px);
          }
          100% { 
            opacity: 1; 
            transform: translateY(0) scale(1);
            filter: blur(0px);
          }
        }
        
        /* Custom scrollbar for Apple feel */
        .package-dropdown div::-webkit-scrollbar {
          width: 6px;
        }
        
        .package-dropdown div::-webkit-scrollbar-track {
          background: transparent;
        }
        
        .package-dropdown div::-webkit-scrollbar-thumb {
          background: rgba(0, 0, 0, 0.2);
          border-radius: 10px;
        }
        
        .package-dropdown div::-webkit-scrollbar-thumb:hover {
          background: rgba(0, 0, 0, 0.3);
        }
        
        @keyframes checkmarkBounce {
          0% { 
            transform: scale(0) rotate(0deg);
            opacity: 0;
          }
          60% { 
            transform: scale(1.2) rotate(10deg);
            opacity: 0.8;
          }
          100% { 
            transform: scale(1) rotate(0deg);
            opacity: 1;
          }
        }
      `}</style>
    </div>
  );
}
  