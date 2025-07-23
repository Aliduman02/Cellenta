import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronRight } from "lucide-react";
import AppleStyleDock from "./components/AppleStyleDock";
import Sidebar from "./components/Sidebar";
import ChatWidget from "./components/ChatWidget";

import apiService from "./services/api";

export default function Store() {
  const [packages, setPackages] = useState([]);
  const [openId, setOpenId] = useState(null);
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
    const loadPackages = async () => {
      try {
        setIsLoading(true);
        const packagesData = await apiService.getPackages();
        
        // API'den gelen veriyi component formatına çevir
        const formattedPackages = packagesData.map(pkg => ({
          id: pkg.package_id,
          name: pkg.packageName,
          price: pkg.price,
          summary: `${pkg.amountMinutes} dakika, ${pkg.amountData} MB, ${pkg.amountSms} SMS`,
          details: [
            `Dakika: ${pkg.amountMinutes} dakika`,
            `İnternet: ${pkg.amountData} MB`,
            `SMS: ${pkg.amountSms} adet`,
            `Süre: ${pkg.period} gün`,
            `Fiyat: ${pkg.price} TL`
          ]
        }));
        
        setPackages(formattedPackages);
      } catch (error) {
        console.error('Failed to load packages:', error);
        setError('Paketler yüklenemedi. Lütfen tekrar deneyin.');
        
        // Token geçersizse login'e yönlendir
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          apiService.removeAuthToken();
          window.location.href = '/login';
        }
      } finally {
        setIsLoading(false);
      }
    };

    loadPackages();
  }, []);

  const handlePurchasePackage = async (packageId) => {
    try {
      const userId = localStorage.getItem('userId');
      await apiService.assignPackageToCustomer(userId, packageId);
      alert('Paket başarıyla satın alındı!');
      window.location.href = '/dashboard';
    } catch (error) {
      console.error('Failed to purchase package:', error);
      alert('Satın alma başarısız. Lütfen tekrar deneyin.');
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
              animation: "spin 1s linear infinite",
              WebkitAnimation: "spin 1s linear infinite",
              MozAnimation: "spin 1s linear infinite",
              msAnimation: "spin 1s linear infinite"
            }}></div>
            Paketler yükleniyor...
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
      {!isDesktop && <Sidebar user={{ name: localStorage.getItem('userName'), phone: localStorage.getItem('userPhone') }} />}

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
          marginBottom: 24,
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
          marginBottom: 32,
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
            🛍️ Mağaza
          </span>
        </div>

        {/* Packages Container */}
        <div style={{ 
          background: "rgba(255,255,255,0.85)", 
          backdropFilter: "blur(10px)",
          borderRadius: 20, 
          boxShadow: "0 6px 24px rgba(0,0,0,0.08)", 
          padding: isDesktop ? 24 : 20,
          border: "1px solid rgba(255,255,255,0.3)",
          animation: "slideInUp 0.8s ease 0.4s both",
          maxWidth: "100%"
        }}>
          {packages.length === 0 ? (
            <div style={{ 
              textAlign: "center", 
              padding: "48px 24px",
              color: "#6b7280",
              fontSize: "16px",
              animation: "fadeIn 1s ease"
            }}>
              📦 Mevcut paket yok
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
              {packages.map((pkg, index) => {
                const isOpen = openId === pkg.id;
                return (
                  <motion.div
                    key={pkg.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: index * 0.1 }}
                    style={{
                      border: "2px solid #e5e7eb",
                      borderRadius: 20,
                      background: isOpen 
                        ? "linear-gradient(135deg, #f5f3ff, #ede9fe)" 
                        : "rgba(255,255,255,0.9)",
                      boxShadow: isOpen 
                        ? "0 8px 32px rgba(124,60,237,0.15), 0 0 0 1px rgba(124,58,237,0.2)" 
                        : "0 4px 16px rgba(0,0,0,0.08)",
                      transition: "all 0.4s cubic-bezier(0.4, 0, 0.2, 1)",
                      overflow: "hidden",
                      position: "relative"
                    }}
                  >
                    {/* Glow effect for opened package */}
                    {isOpen && (
                      <div 
                        style={{
                          position: "absolute",
                          top: -2,
                          left: -2,
                          right: -2,
                          bottom: -2,
                          background: "linear-gradient(45deg, rgba(124,58,237,0.3), rgba(168,85,247,0.3))",
                          borderRadius: 22,
                          zIndex: -1,
                          animation: "pulse 2s infinite"
                        }}
                      />
                    )}

                    <button
                      onClick={() => setOpenId(isOpen ? null : pkg.id)}
                      style={{
                        width: "100%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        background: "none",
                        border: "none",
                        padding: "24px 28px",
                        cursor: "pointer",
                        fontSize: 18,
                        fontWeight: 700,
                        color: isOpen ? "#7c3aed" : "#1f2937",
                        outline: "none",
                        transition: "all 0.3s ease"
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.background = "rgba(124,58,237,0.05)";
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.background = "none";
                      }}
                    >
                      <div style={{ textAlign: "left" }}>
                        <div style={{ fontWeight: 700, fontSize: 20, marginBottom: 4 }}>
                          📦 {pkg.name}
                        </div>
                        <div style={{ 
                          fontWeight: 500, 
                          fontSize: 15, 
                          color: "#64748b", 
                          marginTop: 4 
                        }}>
                          {pkg.summary}
                        </div>
                      </div>
                      <motion.div
                        animate={{ rotate: isOpen ? 90 : 0 }}
                        transition={{ duration: 0.3, ease: "easeInOut" }}
                        style={{ 
                          display: "flex", 
                          alignItems: "center", 
                          justifyContent: "center", 
                          background: isOpen ? "#ede9fe" : "#f3f4f6", 
                          borderRadius: "50%", 
                          width: 40, 
                          height: 40,
                          transition: "all 0.3s ease"
                        }}
                      >
                        <ChevronRight size={20} color={isOpen ? "#7c3aed" : "#6b7280"} />
                      </motion.div>
                    </button>

                    <AnimatePresence initial={false}>
                      {isOpen && (
                        <motion.div
                          initial={{ height: 0, opacity: 0 }}
                          animate={{ height: "auto", opacity: 1 }}
                          exit={{ height: 0, opacity: 0 }}
                          transition={{ duration: 0.4, ease: "easeInOut" }}
                          style={{ overflow: "hidden" }}
                        >
                          <div style={{ 
                            background: "rgba(248,250,252,0.8)", 
                            padding: "0 28px 28px 28px",
                            borderTop: "1px solid rgba(148,163,184,0.2)"
                          }}>
                            <div style={{ 
                              fontWeight: 700, 
                              fontSize: 20, 
                              margin: "20px 0 12px 0", 
                              color: "#1f2937",
                              display: "flex",
                              alignItems: "center",
                              gap: 8
                            }}>
                              ✨ {pkg.name} Detayları
                            </div>
                            <ul style={{ 
                              listStyle: "none", 
                              padding: 0, 
                              margin: 0, 
                              fontSize: 16, 
                              color: "#475569" 
                            }}>
                              {pkg.details.map((detail, i) => (
                                <motion.li
                                  key={i}
                                  initial={{ opacity: 0, x: -20 }}
                                  animate={{ opacity: 1, x: 0 }}
                                  transition={{ duration: 0.3, delay: i * 0.1 }}
                                  style={{ 
                                    marginBottom: 8, 
                                    padding: "8px 16px",
                                    background: "rgba(255,255,255,0.6)",
                                    borderRadius: 12,
                                    border: "1px solid rgba(226,232,240,0.8)",
                                    display: "flex",
                                    alignItems: "center",
                                    gap: 8
                                  }}
                                >
                                  <span style={{ color: "#7c3aed", fontSize: "14px" }}>•</span>
                                  {detail}
                                </motion.li>
                              ))}
                            </ul>
                            <motion.button
                              whileHover={{ scale: 1.02 }}
                              whileTap={{ scale: 0.98 }}
                              onClick={() => handlePurchasePackage(pkg.id)}
                              style={{
                                marginTop: 20,
                                width: "100%",
                                background: "linear-gradient(135deg, #7c3aed, #a855f7)",
                                color: "#fff",
                                border: "none",
                                borderRadius: 16,
                                padding: "16px 0",
                                fontWeight: 600,
                                fontSize: 16,
                                cursor: "pointer",
                                transition: "all 0.3s ease",
                                boxShadow: "0 4px 16px rgba(124,58,237,0.3)",
                                position: "relative",
                                overflow: "hidden"
                              }}
                              onMouseEnter={(e) => {
                                e.currentTarget.style.boxShadow = "0 6px 24px rgba(124,58,237,0.4)";
                                e.currentTarget.style.transform = "translateY(-2px)";
                              }}
                              onMouseLeave={(e) => {
                                e.currentTarget.style.boxShadow = "0 4px 16px rgba(124,58,237,0.3)";
                                e.currentTarget.style.transform = "translateY(0)";
                              }}
                            >
                              🛒 Paketi Seç
                            </motion.button>
                          </div>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </motion.div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      <ChatWidget />

      <style jsx>{`
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
        
        @keyframes slideInUp {
          from { opacity: 0; transform: translateY(50px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes pulse {
          0%, 100% { opacity: 0.5; }
          50% { opacity: 1; }
        }

        @media (max-width: 767px) {
          .packages-container {
            margin: 0 16px;
            padding: 24px 20px !important;
          }
        }
      `}</style>
    </div>
  );
} 