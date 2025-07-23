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
        setError('Failed to load profile. Please try again.');
        
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
              width: isDesktop ? "20px" : "18px",
              height: isDesktop ? "20px" : "18px",
              border: `${isDesktop ? "2px" : "2px"} solid #e5e7eb`,
              borderTop: `${isDesktop ? "2px" : "2px"} solid #7c3aed`,
              borderRadius: "50%",
              animation: "spin 1s linear infinite"
            }}></div>
            Loading profile...
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
            Error
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
            Try Again
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
        maxWidth: isDesktop ? 850 : "100%", 
        margin: "0 auto", 
        padding: isDesktop ? "24px 40px 40px 40px" : "50px 16px 32px 16px",
        width: "100%"
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
            👤 Profile
          </span>
        </div>

        {/* Profile Card */}
        <div style={{
          display: "flex",
          alignItems: isDesktop ? "center" : "flex-start",
          flexDirection: isDesktop ? "row" : "column",
          gap: isDesktop ? 60 : 20,
          background: "#fff",
          borderRadius: isDesktop ? 32 : 20,
          boxShadow: "0 6px 24px rgba(0,0,0,0.08)",
          padding: isDesktop ? "48px 60px" : "32px 24px",
          maxWidth: "100%",
          margin: "0 auto",
          minHeight: isDesktop ? 280 : "auto",
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
            🚪 Logout
          </button>

          <div 
            style={{
              width: isDesktop ? 120 : 80,
              height: isDesktop ? 120 : 80,
              borderRadius: "50%",
              background: "linear-gradient(135deg, #22d3ee, #06b6d4)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: isDesktop ? 48 : 32,
              fontWeight: 800,
              color: "#fff",
              boxShadow: "0 4px 16px rgba(0,0,0,0.12)",
              border: isDesktop ? "6px solid #e0e7ef" : "4px solid #e0e7ef",
              transition: "transform 0.3s ease",
              animation: "bounceIn 1s ease 0.6s both",
              alignSelf: isDesktop ? "auto" : "center"
            }}
            onMouseOver={e => e.currentTarget.style.transform = "scale(1.1)"}
            onMouseOut={e => e.currentTarget.style.transform = "scale(1)"}
          >
            {user?.firstName ? user.firstName[0] : "U"}
          </div>
          
          <div style={{
            display: "flex",
            flexDirection: "column",
            gap: isDesktop ? 20 : 16,
            width: "100%",
            alignItems: isDesktop ? "flex-start" : "center"
          }}>
            <div style={{
              display: "flex",
              alignItems: "center",
              gap: isDesktop ? 16 : 12,
              fontSize: isDesktop ? 18 : 16,
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "0.8s",
              flexDirection: isDesktop ? "row" : "column",
              textAlign: isDesktop ? "left" : "center"
            }}>
              <span style={{
                color: "#888",
                fontWeight: 700,
                minWidth: isDesktop ? 120 : "auto",
                fontSize: isDesktop ? 16 : 14
              }}>👤 Name</span>
              <span style={{
                fontWeight: 800,
                color: "#1f2937",
                fontSize: isDesktop ? 18 : 16
              }}>{user?.firstName || "N/A"}</span>
            </div>
            <div style={{
              display: "flex",
              alignItems: "center",
              gap: isDesktop ? 16 : 12,
              fontSize: isDesktop ? 18 : 16,
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "1.0s",
              flexDirection: isDesktop ? "row" : "column",
              textAlign: isDesktop ? "left" : "center"
            }}>
              <span style={{
                color: "#888",
                fontWeight: 700,
                minWidth: isDesktop ? 120 : "auto",
                fontSize: isDesktop ? 16 : 14
              }}>👥 Surname</span>
              <span style={{
                fontWeight: 800,
                color: "#1f2937",
                fontSize: isDesktop ? 18 : 16
              }}>{user?.lastName || "N/A"}</span>
            </div>
            <div style={{
              display: "flex",
              alignItems: "center",
              gap: isDesktop ? 16 : 12,
              fontSize: isDesktop ? 18 : 16,
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "1.2s",
              flexDirection: isDesktop ? "row" : "column",
              textAlign: isDesktop ? "left" : "center"
            }}>
              <span style={{
                color: "#888",
                fontWeight: 700,
                minWidth: isDesktop ? 120 : "auto",
                fontSize: isDesktop ? 16 : 14
              }}>📱 Phone</span>
              <span style={{
                fontWeight: 800,
                color: "#1f2937",
                fontSize: isDesktop ? 18 : 16
              }}>{user?.phone || "N/A"}</span>
            </div>
            <div style={{
              display: "flex",
              alignItems: "center",
              gap: isDesktop ? 16 : 12,
              fontSize: isDesktop ? 18 : 16,
              opacity: 0,
              animation: "fadeInLeft 0.6s ease forwards",
              animationDelay: "1.4s",
              flexDirection: isDesktop ? "row" : "column",
              textAlign: isDesktop ? "left" : "center"
            }}>
              <span style={{
                color: "#888",
                fontWeight: 700,
                minWidth: isDesktop ? 120 : "auto",
                fontSize: isDesktop ? 16 : 14
              }}>✉️ Email</span>
              <span style={{
                fontWeight: 800,
                color: "#1f2937",
                fontSize: isDesktop ? 18 : 16
              }}>{user?.email || "N/A"}</span>
            </div>
          </div>
        </div>
      </div>

      <ChatWidget />

      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
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