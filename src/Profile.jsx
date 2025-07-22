import React, { useState, useEffect } from "react";
import AppleStyleDock from "./components/AppleStyleDock";
import ChatWidget from "./components/ChatWidget";
import apiService from "./services/api";

export default function Profile() {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showPassword, setShowPassword] = useState(false);

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
          padding: "32px", 
          borderRadius: "16px",
          textAlign: "center"
        }}>
          <div style={{ fontSize: "18px", fontWeight: 600, color: "#374151" }}>
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
          padding: "32px", 
          borderRadius: "16px",
          textAlign: "center",
          maxWidth: "400px"
        }}>
          <div style={{ fontSize: "18px", fontWeight: 600, color: "#ef4444", marginBottom: "16px" }}>
            Error
          </div>
          <div style={{ color: "#6b7280", marginBottom: "24px" }}>
            {error}
          </div>
          <button
            onClick={() => window.location.reload()}
            style={{
              background: "#7c3aed",
              color: "#fff",
              border: "none",
              borderRadius: "8px",
              padding: "12px 24px",
              cursor: "pointer",
              fontWeight: 600
            }}
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
      }}
    >
      <AppleStyleDock />
      {/* Log out button fixed top right */}
      <button
        onClick={handleLogout}
        style={{
          position: "fixed",
          top: 32,
          right: 48,
          background: "#ffb3b3",
          color: "#fff",
          border: "none",
          borderRadius: 24,
          padding: "12px 32px",
          fontWeight: 700,
          fontSize: 17,
          cursor: "pointer",
          boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
          zIndex: 100,
          transition: "background 0.2s",
        }}
        onMouseOver={e => (e.currentTarget.style.background = '#ff8a8a')}
        onMouseOut={e => (e.currentTarget.style.background = '#ffb3b3')}
      >
        Log out
      </button>
      <div style={{ maxWidth: 900, margin: "0 auto", padding: "48px 0 0 0" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 32 }}>
          <div className="header-logo" style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
            <img src="/images/title2.png" alt="Cellenta" height={48} style={{ height: 48, width: "auto", objectFit: "contain" }} />
          </div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 32 }}>
          <span style={{ fontSize: 28, fontWeight: 700, color: "#222" }}>Profile</span>
        </div>
        <div className="profile-card">
          <div className="profile-avatar">
            {user?.firstName ? user.firstName[0] : "U"}
          </div>
          <div className="profile-info-list">
            <div className="profile-info-row">
              <span className="profile-info-label">Name</span>
              <span className="profile-info-value">{user?.firstName || "N/A"}</span>
            </div>
            <div className="profile-info-row">
              <span className="profile-info-label">Surname</span>
              <span className="profile-info-value">{user?.lastName || "N/A"}</span>
            </div>
            <div className="profile-info-row">
              <span className="profile-info-label">Phone</span>
              <span className="profile-info-value">{user?.phone || "N/A"}</span>
            </div>
            <div className="profile-info-row">
              <span className="profile-info-label">Email</span>
              <span className="profile-info-value">{user?.email || "N/A"}</span>
            </div>
          </div>
        </div>
      </div>
      <ChatWidget />
    </div>
  );
} 