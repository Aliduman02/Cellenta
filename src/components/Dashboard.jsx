import React, { useState, useEffect } from "react";
import Header from "./Header";
import TariffInfo from "./TariffInfo";
import ChatWidget from "./ChatWidget";
import AppleStyleDock from "./AppleStyleDock";
import Sidebar from "./Sidebar";
import Store from "../Store";
import apiService from "../services/api";

import "../Dashboard.css";

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [activeTariff, setActiveTariff] = useState(null);
  const [usageData, setUsageData] = useState(null);
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

    loadDashboardData();
  }, []);

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
            Anasayfa yükleniyor...
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
      className="dashboard-container"
      style={{
        backgroundImage: "url('/images/bg.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        minHeight: "100vh",
        width: "100vw",
        margin: 0,
        padding: 0,
      }}
    >
      {/* AppleStyleDock only for large screens */}
      {isDesktop && <AppleStyleDock />}

      <div className="dashboard-layout">
        {/* Sidebar for medium and small screens */}
        {!isDesktop && <Sidebar user={user} />}
        
        <div className="dashboard-content" style={{ 
          paddingTop: !isDesktop ? "60px" : "0",
          transition: "all 0.3s ease"
        }}>

                    <Header user={user} activeTariff={activeTariff} />
          <main className="dashboard-main">
            <TariffInfo usageData={usageData} />
            <ChatWidget />
          </main>
        </div>
      </div>

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
        
        @keyframes shimmer {
          0% { left: -100%; }
          100% { left: 100%; }
        }
      `}</style>
    </div>
  );
}