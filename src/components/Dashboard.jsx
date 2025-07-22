import React, { useState, useEffect } from "react";
import Header from "./Header";
import TariffInfo from "./TariffInfo";
import ChatWidget from "./ChatWidget";
import AppleStyleDock from "./AppleStyleDock";
import Store from "../Store";
import apiService from "../services/api";
import "../Dashboard.css";

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [activeTariff, setActiveTariff] = useState(null);
  const [usageData, setUsageData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setIsLoading(true);
        
        // Her API çağrısını ayrı ayrı yap ve hata durumunda boş değer kullan
        let userProfile = null;
        let tariffData = null;
        let usageData = null;

        try {
          userProfile = await apiService.getUserProfile();
        } catch (error) {
          console.error('getUserProfile failed:', error);
          userProfile = { name: '', surname: '', email: '', msisdn: '' };
        }

        try {
          tariffData = await apiService.getActiveTariff();
        } catch (error) {
          console.error('getActiveTariff failed:', error);
          tariffData = { name: '', price: 0, period: 0 };
        }

        try {
          usageData = await apiService.getUsageData();
        } catch (error) {
          console.error('getUsageData failed:', error);
          usageData = { remainingMinutes: 0, remainingData: 0, remainingSms: 0 };
        }

        // API response'ları direkt kullan
        setUser(userProfile);
        setActiveTariff(tariffData);
        setUsageData(usageData);
        
      } catch (error) {
        console.error('Failed to load dashboard data:', error);
        setError('Failed to load dashboard data. Please try again.');
        
        // Token geçersizse login'e yönlendir
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          apiService.removeAuthToken();
          window.location.href = '/login';
        }
      } finally {
        setIsLoading(false);
      }
    };

    loadDashboardData();
  }, []);

  // Paket yoksa store'a yönlendirme kaldırıldı
  // if (!isLoading && activeTariff && !activeTariff.name) {
  //   window.location.href = '/store';
  //   return null;
  // }

  // Basit route: /store ise Store, değilse dashboard
  if (window.location.pathname === "/store") {
    return <Store />;
  }

  if (isLoading) {
    return (
      <div
        className="dashboard-container"
        style={{
          backgroundImage: "url('/images/bg.jpg')",
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          minHeight: "100vh",
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
            Loading dashboard...
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div
        className="dashboard-container"
        style={{
          backgroundImage: "url('/images/bg.jpg')",
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          minHeight: "100vh",
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
      className="dashboard-container"
      style={{
        backgroundImage: "url('/images/bg.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        minHeight: "100vh",
      }}
    >
      {/* Log out button fixed top right */}
      
      <AppleStyleDock />
      <div className="dashboard-layout">
        <div className="dashboard-content">
          {/* Paket yoksa uyarı göster */}
          {(!activeTariff || !activeTariff.name) && (
            <div style={{
              background: '#fff0f0',
              color: '#b91c1c',
              border: '1px solid #fecaca',
              borderRadius: 12,
              padding: '18px 32px',
              marginBottom: 32,
              fontSize: 20,
              fontWeight: 700,
              textAlign: 'center',
              boxShadow: '0 2px 8px rgba(255,0,0,0.04)'
            }}>
              Package not found. Please select a package.
            </div>
          )}
          <Header user={user} activeTariff={activeTariff} />
          <main className="dashboard-main">
            <TariffInfo usageData={usageData} />
            <ChatWidget />
          </main>
        </div>
      </div>
    </div>
  );
}