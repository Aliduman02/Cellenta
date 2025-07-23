import React, { useState } from 'react';
import './LoginPage.css';
import { LeftPanel } from './LeftPanel';
import { LoginForm } from './LoginForm';
import { SignUpStep1 } from './SignUpStep1';
import { SignUpStep2 } from './SignUpStep2';

import { CSSTransition, SwitchTransition } from 'react-transition-group';

export default function LoginPage() {
  const [step, setStep] = useState('login');
  const [userData, setUserData] = useState(null);
  const [passwordData, setPasswordData] = useState(null);

  const handleSignUpClick = () => setStep('signup1');
  const handleBackToLogin = () => {
    setStep('login');
    setUserData(null);
    setPasswordData(null);
  };
  const handleContinue = (data) => {
    // Eğer passwordData varsa (step1'den step2'ye dönüş), şifre verilerini geri yükle
    if (data.passwordData) {
      setPasswordData(data.passwordData);
      delete data.passwordData; // userData'dan çıkar
    }
    setUserData(data);
    setStep('signup2');
  };
  const handleBackToStep1 = (currentPasswordData) => {
    setPasswordData(currentPasswordData); // Şifre verilerini koru
    setStep('signup1');
  };

  return (
    <div 
      className="login-container"
      style={{
        backgroundImage: "url('/images/background-gradient.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        backgroundAttachment: "fixed",
        minHeight: "100vh",
        width: "100vw"
      }}
    >
      <div className="left-panel">
        <LeftPanel />
      </div>

      <div className="right-panel">
        <SwitchTransition mode="out-in">
          <CSSTransition key={step} classNames="form-slide" timeout={300}>
            <div>
              {step === 'login' && <LoginForm onSignUpClick={handleSignUpClick} />}
              {step === 'signup1' && (
                <SignUpStep1 
                  onNext={handleContinue} 
                  onBack={handleBackToLogin}
                  userData={userData}
                  passwordData={passwordData}
                />
              )}
              {step === 'signup2' && (
                <SignUpStep2 
                  onBack={handleBackToLogin} 
                  onBackToStep1={handleBackToStep1}
                  userData={userData}
                  passwordData={passwordData}
                />
              )}
            </div>
          </CSSTransition>
        </SwitchTransition>
      </div>
    </div>
  );
}
