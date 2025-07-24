import React, { useState, useEffect } from "react";
import AppleStyleDock from "./components/AppleStyleDock";
import Sidebar from "./components/Sidebar";
import ChatWidget from "./components/ChatWidget";

import apiService from "./services/api";

export default function Profile() {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isDesktop, setIsDesktop] = useState(false);

  // Responsive check for AppleStyleDock
  useEffect(() => {
    const checkScreenSize = () => {
      setIsDesktop(window.innerWidth >= 1200);
    };

    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        setIsLoading(true);
        const userData = await apiService.getUserProfile();
        
        // API'den gelen veriyi component formatına çevir
        const formattedUser = {
          firstName: userData.name || userData.firstName,
          lastName: userData.surname || userData.lastName,
          phone: userData.msisdn || userData.phone,
          email: userData.email
        };
        
        setUser(formattedUser);
      } catch (error) {
        console.error('Failed to load user profile:', error);
        setError('Veri yüklenemedi');
        
        // Token geçersizse login'e yönlendir
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          apiService.removeAuthToken();
          window.location.href = '/login';
        }
      } finally {
        setIsLoading(false);
      }
    };

    loadUserProfile();
  }, []);

  const handleLogout = async () => {
    try {
      await apiService.logout();
    } catch (error) {
      console.error('Logout failed:', error);
      // Yine de token'ı sil ve login'e yönlendir
      apiService.removeAuthToken();
      window.location.href = '/login';
    }
  };

  if (isLoading) {
    return (
      <div
        style={{
          minHeight: "100vh",
          backgroundImage: "url('/images/bg.jpg')",
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          position: "relative",
          display: "flex",
          alignItems: "center",
          justifyContent: "center"
        }}
      >
        <div style={{ 
          background: "rgba(255,255,255,0.9)", 
          padding: isDesktop ? "32px" : "24px", 
          borderRadius: "16px",
          textAlign: "center",
          boxShadow: "0 8px 32px rgba(0,0,0,0.1)",
          animation: "fadeIn 0.6s ease",
          maxWidth: isDesktop ? "400px" : "320px",
          margin: "0 16px"
        }}>
          <div style={{ 
            fontSize: isDesktop ? "18px" : "16px", 
            fontWeight: 600, 
            color: "#374151",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: isDesktop ? "12px" : "10px"
          }}>
            <div style={{
              width: "16px",
              height: "16px",
              border: "2px solid rgba(124,58,237,0.3)",
              borderTop: "2px solid #7c3aed",
              borderRadius: "50%",
              animation: "spin 1s linear infinite"
            }}></div>
            Profil yükleniyor...
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div
        style={{
          minHeight: "100vh",
          backgroundImage: "url('/images/bg.jpg')",
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          position: "relative",
          display: "flex",
          alignItems: "center",
          justifyContent: "center"
        }}
      >
        <div style={{ 
          background: "rgba(255,255,255,0.9)", 
          padding: isDesktop ? "32px" : "24px", 
          borderRadius: "16px",
          textAlign: "center",
          maxWidth: isDesktop ? "400px" : "320px",
          boxShadow: "0 8px 32px rgba(0,0,0,0.1)",
          animation: "fadeIn 0.6s ease",
          margin: "0 16px"
        }}>
          <div style={{ 
            fontSize: isDesktop ? "18px" : "16px", 
            fontWeight: 600, 
            color: "#ef4444", 
            marginBottom: isDesktop ? "16px" : "12px" 
          }}>
            Hata
          </div>
          <div style={{ 
            color: "#6b7280", 
            marginBottom: isDesktop ? "24px" : "20px",
            fontSize: isDesktop ? "14px" : "13px"
          }}>
            {error}
          </div>
          <button
            onClick={() => window.location.reload()}
            style={{
              background: "#7c3aed",
              color: "#fff",
              border: "none",
              borderRadius: "8px",
              padding: isDesktop ? "12px 24px" : "10px 20px",
              cursor: "pointer",
              fontWeight: 600,
              fontSize: isDesktop ? "14px" : "13px",
              transition: "all 0.3s ease"
            }}
            onMouseOver={e => e.target.style.background = "#6d28d9"}
            onMouseOut={e => e.target.style.background = "#7c3aed"}
          >
            Tekrar Dene
          </button>
        </div>
      </div>
    );
  }

  return (
    <div
      style={{
        minHeight: "100vh",
        backgroundImage: "url('/images/bg.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        position: "relative",
        width: "100vw",
        margin: 0,
        padding: 0,
      }}
    >
      {/* AppleStyleDock only for large screens */}
      {isDesktop && <AppleStyleDock />}
      
      {/* Sidebar for medium and small screens */}
      {!isDesktop && <Sidebar user={user} />}

      <div style={{ 
        maxWidth: isDesktop ? 900 : "100%", 
        margin: "0 auto", 
        padding: isDesktop ? "40px 40px 80px 40px" : "50px 16px 32px 16px",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
      }}>
        {/* Header Logo */}
        <div style={{ 
          display: "flex", 
          alignItems: "center", 
          justifyContent: "center", 
          marginBottom: isDesktop ? 32 : 24,
          animation: "fadeInDown 0.8s ease"
        }}>
          <div style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
            <img 
              src="/images/title2.png" 
              alt="Cellenta" 
              style={{ 
                height: isDesktop ? 48 : 40, 
                width: "auto", 
                objectFit: "contain",
                transition: "transform 0.3s ease"
              }}
              onMouseOver={e => e.target.style.transform = "scale(1.05)"}
              onMouseOut={e => e.target.style.transform = "scale(1)"}
            />
          </div>
        </div>

        {/* Page Title */}
        <div style={{ 
          display: "flex", 
          alignItems: "center",
          justifyContent: "center",
          marginBottom: isDesktop ? 40 : 32,
          animation: "slideInLeft 0.8s ease 0.2s both",
          width: "100%"
        }}>
          <span style={{ 
            fontSize: isDesktop ? 36 : 28, 
            fontWeight: 800, 
            color: "#1f2937",
            textShadow: "0 3px 6px rgba(0,0,0,0.15)",
            letterSpacing: "-0.5px"
          }}>
            👤 Profil
          </span>
        </div>

        {/* Profile Card */}
        <div className="profile-card" style={{
          animation: "slideInUp 0.8s ease 0.4s both",
          position: "relative"
        }}>
          {/* Logout Button */}
          <button
            onClick={handleLogout}
            style={{
              position: "absolute",
              top: isDesktop ? 20 : 16,
              right: isDesktop ? 20 : 16,
              background: "linear-gradient(135deg, #ff6b6b, #ee5a52)",
              color: "#fff",
              border: "none",
              borderRadius: isDesktop ? 14 : 12,
              padding: isDesktop ? "10px 16px" : "8px 12px",
              fontWeight: 600,
              fontSize: isDesktop ? 13 : 12,
              cursor: "pointer",
              boxShadow: "0 3px 12px rgba(238, 90, 82, 0.3)",
              transition: "all 0.3s ease",
              zIndex: 10,
              display: "flex",
              alignItems: "center",
              gap: isDesktop ? 6 : 4,
              animation: "fadeInRight 1s ease 1.6s both"
            }}
            onMouseOver={e => {
              e.currentTarget.style.background = "linear-gradient(135deg, #ee5a52, #dc4437)";
              e.currentTarget.style.transform = "translateY(-2px)";
              e.currentTarget.style.boxShadow = "0 6px 20px rgba(238, 90, 82, 0.4)";
            }}
            onMouseOut={e => {
              e.currentTarget.style.background = "linear-gradient(135deg, #ff6b6b, #ee5a52)";
              e.currentTarget.style.transform = "translateY(0)";
              e.currentTarget.style.boxShadow = "0 3px 12px rgba(238, 90, 82, 0.3)";
            }}
          >
            🚪 Çıkış Yap
          </button>

          <div 
            className="profile-avatar"
            style={{
              background: "linear-gradient(135deg, #22d3ee, #06b6d4)",
              transition: "transform 0.3s ease",
              animation: "bounceIn 1s ease 0.6s both"
            }}
            onMouseOver={e => e.currentTarget.style.transform = "scale(1.1)"}
            onMouseOut={e => e.currentTarget.style.transform = "scale(1)"}
          >
            {user?.firstName ? user.firstName[0] : "U"}
          </div>
          
          <div className="profile-info-list">
            <div className="profile-info-row" style={{
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "0.8s"
            }}>
              <span className="profile-info-label">👤 Ad</span>
              <span className="profile-info-value">{user?.firstName || "N/A"}</span>
            </div>
            <div className="profile-info-row" style={{
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "1.0s"
            }}>
              <span className="profile-info-label">👥 Soyad</span>
              <span className="profile-info-value">{user?.lastName || "N/A"}</span>
            </div>
            <div className="profile-info-row" style={{
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "1.2s"
            }}>
              <span className="profile-info-label">📱 Telefon</span>
              <span className="profile-info-value">{user?.phone || "N/A"}</span>
            </div>
            <div className="profile-info-row" style={{
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "1.4s"
            }}>
              <span className="profile-info-label">✉️ E-posta</span>
              <span className="profile-info-value">{user?.email || "N/A"}</span>
            </div>
          </div>
        </div>
      </div>

      <ChatWidget />

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
        
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes fadeInDown {
          from { opacity: 0; transform: translateY(-30px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes slideInLeft {
          from { opacity: 0; transform: translateX(-30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        
        @keyframes slideInRight {
          from { opacity: 0; transform: translateX(30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        
        @keyframes fadeInRight {
          from { opacity: 0; transform: translateX(30px) scale(0.8); }
          to { opacity: 1; transform: translateX(0) scale(1); }
        }
        
        @keyframes slideInUp {
          from { opacity: 0; transform: translateY(50px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes bounceIn {
          0% { opacity: 0; transform: scale(0.3); }
          50% { opacity: 1; transform: scale(1.1); }
          70% { transform: scale(0.9); }
          100% { opacity: 1; transform: scale(1); }
        }
        
        @keyframes fadeInLeft {
          from {
            opacity: 0;
            transform: translateX(-20px);
          }
          to {
            opacity: 1;
            transform: translateX(0);
          }
        }

        @media (max-width: 767px) {
          .profile-card {
            margin: 0 16px;
            padding: 32px 24px !important;
          }
        }
      `}</style>
    </div>
  );
} 