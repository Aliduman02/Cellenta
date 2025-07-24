import React, { useState, useEffect } from "react";
import { ChevronRight } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
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
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [selectedPackageForPurchase, setSelectedPackageForPurchase] = useState(null);
  const [isPurchasing, setIsPurchasing] = useState(false);
  const [usageData, setUsageData] = useState(null);

  // Responsive check for AppleStyleDock
  useEffect(() => {
    const checkScreenSize = () => {
      setIsDesktop(window.innerWidth >= 1200);
    };

    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

  // Kullanım verisini çek
  useEffect(() => {
    apiService.getUsageData().then(setUsageData).catch(() => setUsageData(null));
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

  const handleShowConfirmDialog = (pkg) => {
    setSelectedPackageForPurchase(pkg);
    setShowConfirmDialog(true);
  };

  const handleConfirmPurchase = async () => {
    if (!selectedPackageForPurchase) return;
    
    setIsPurchasing(true);
    try {
      const userId = localStorage.getItem('userId');
      await apiService.assignPackageToCustomer(userId, selectedPackageForPurchase.id);
      setShowConfirmDialog(false);
      setSelectedPackageForPurchase(null);
      
      // Başarı mesajı göster
      setTimeout(() => {
        window.location.href = '/dashboard';
      }, 1000);
      
    } catch (error) {
      console.error('Failed to purchase package:', error);
      alert('Satın alma başarısız. Lütfen tekrar deneyin.');
    } finally {
      setIsPurchasing(false);
    }
  };

  const handleCancelPurchase = () => {
    setShowConfirmDialog(false);
    setSelectedPackageForPurchase(null);
  };

  // Kişiselleştirilmiş öneri algoritması
  function getPersonalizedRecommendations(usage, packages) {
    if (!usage || !packages.length) return [];

    // Starter önerisi: hiç paketi yoksa
    if (
      (!usage.totalData && !usage.totalMinutes && !usage.totalSms) ||
      (usage.totalData === 0 && usage.totalMinutes === 0 && usage.totalSms === 0)
    ) {
      const starterNames = ["Mini Öğrenci", "Mini Konuşma", "Mini İnternet"];
      return packages.filter(pkg => starterNames.includes(pkg.name)).slice(0, 2);
    }

    // Kullanım yüzdeleri
    const dataPercent = usage.totalData ? (usage.totalData - usage.remainingData) / usage.totalData * 100 : 0;
    const minPercent = usage.totalMinutes ? (usage.totalMinutes - usage.remainingMinutes) / usage.totalMinutes * 100 : 0;
    const smsPercent = usage.totalSms ? (usage.totalSms - usage.remainingSms) / usage.totalSms * 100 : 0;

    let recs = [];
    if (dataPercent > 70) {
      const dataPkgs = ["Süper İnternet", "Full Paket", "Sosyal Medya Paketi"];
      recs.push(...packages.filter(pkg => dataPkgs.includes(pkg.name)));
    }
    if (minPercent > 70) {
      const minPkgs = ["Mega Konuşma", "Aile Paketi", "Full Paket"];
      recs.push(...packages.filter(pkg => minPkgs.includes(pkg.name)));
    }
    // Duplicates kaldır, en fazla 3 öneri
    return Array.from(new Set(recs)).slice(0, 3);
  }

  if (isLoading) {
    return (
      <div
        style={{
          minHeight: "100vh",
          backgroundImage: "url('/images/bg.jpg')",
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          backgroundAttachment: "fixed",
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
          backgroundAttachment: "fixed",
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
        backgroundAttachment: "fixed",
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

        {/* Sana Özel Öneriler */}
        {usageData && packages.length > 0 && (() => {
          const recs = getPersonalizedRecommendations(usageData, packages);
          if (!recs.length) return null;
          return (
            <div
              style={{
                background: "linear-gradient(90deg, #f9fafc 60%, #a5b4fc 100%)",
                border: "2px solid #6366f1",
                borderRadius: 20,
                boxShadow: "0 6px 24px rgba(99,102,241,0.12)",
                padding: "28px 24px",
                marginBottom: 32,
                animation: "pulse 2s infinite alternate",
                display: "flex",
                flexDirection: "column",
                alignItems: "center"
              }}
            >
              <div style={{
                fontSize: 22,
                fontWeight: 800,
                color: "#3730a3",
                marginBottom: 12,
                letterSpacing: "-0.5px"
              }}>
                🎯 Sana Özel Paket Önerileri
              </div>
              <div style={{ display: "flex", gap: 18, flexWrap: "wrap", justifyContent: "center" }}>
                {recs.map(pkg => (
                  <div
                    key={pkg.id}
                    style={{
                      background: "#fff",
                      border: "2px solid #a5b4fc",
                      borderRadius: 14,
                      padding: "16px 20px",
                      minWidth: 180,
                      boxShadow: "0 2px 8px rgba(99,102,241,0.10)",
                      fontWeight: 700,
                      color: "#3730a3",
                      cursor: "pointer",
                      transition: "transform 0.2s",
                      textAlign: "center"
                    }}
                    onClick={() => {
                      setSelectedPackageForPurchase(pkg);
                      setShowConfirmDialog(true);
                    }}
                    onMouseEnter={e => e.currentTarget.style.transform = "scale(1.04)"}
                    onMouseLeave={e => e.currentTarget.style.transform = "scale(1)"}
                  >
                    <div style={{ fontSize: 18, marginBottom: 6 }}>{pkg.name}</div>
                    <div style={{ fontSize: 15, color: "#6366f1", fontWeight: 500 }}>{pkg.summary}</div>
                    <div style={{ fontSize: 16, color: "#059669", fontWeight: 800, marginTop: 4 }}>{pkg.price} TL</div>
                  </div>
                ))}
              </div>
            </div>
          );
        })()}

        {/* Packages Container */}
        <div style={{ 
          background: "#ffffff", 
          borderRadius: 20, 
          boxShadow: "0 4px 12px rgba(0,0,0,0.08)", 
          padding: isDesktop ? 24 : 20,
          border: "1px solid #e5e7eb",
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
                  <div
                    key={pkg.id}
                    style={{
                      border: "1px solid #e5e7eb",
                      borderRadius: 12,
                      background: "#ffffff",
                      boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
                      overflow: "hidden",
                      position: "relative"
                    }}
                  >


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
                      <div
                        style={{ 
                          display: "flex", 
                          alignItems: "center", 
                          justifyContent: "center", 
                          background: "#f3f4f6", 
                          borderRadius: "50%", 
                          width: 40, 
                          height: 40
                        }}
                      >
                        <ChevronRight size={20} color="#6b7280" style={{ transform: isOpen ? "rotate(90deg)" : "rotate(0deg)" }} />
                      </div>
                    </button>

                    <AnimatePresence>
                      {isOpen && (
                        <motion.div
                          initial={{ 
                            opacity: 0,
                            height: 0,
                            scaleY: 0
                          }}
                          animate={{ 
                            opacity: 1,
                            height: "auto",
                            scaleY: 1
                          }}
                          exit={{ 
                            opacity: 0,
                            height: 0,
                            scaleY: 0
                          }}
                          transition={{
                            duration: 0.4,
                            ease: [0.04, 0.62, 0.23, 0.98],
                            opacity: { duration: 0.25 }
                          }}
                          style={{ 
                            overflow: "hidden",
                            originY: 0
                          }}
                        >
                          <motion.div 
                            initial={{ 
                              opacity: 0,
                              y: -20
                            }}
                            animate={{ 
                              opacity: 1,
                              y: 0
                            }}
                            exit={{ 
                              opacity: 0,
                              y: -10
                            }}
                            transition={{
                              duration: 0.3,
                              delay: 0.1,
                              ease: [0.25, 0.46, 0.45, 0.94]
                            }}
                            style={{ 
                              background: "#f8fafc", 
                              padding: "0 28px 28px 28px",
                              borderTop: "1px solid #e2e8f0"
                            }}
                          >
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
                                  initial={{ 
                                    opacity: 0,
                                    x: -20,
                                    scale: 0.95
                                  }}
                                  animate={{ 
                                    opacity: 1,
                                    x: 0,
                                    scale: 1
                                  }}
                                  transition={{
                                    duration: 0.3,
                                    delay: 0.2 + (i * 0.1),
                                    ease: [0.25, 0.46, 0.45, 0.94]
                                  }}
                                  whileHover={{
                                    scale: 1.02,
                                    x: 4,
                                    transition: { duration: 0.2 }
                                  }}
                                  style={{ 
                                    marginBottom: 8, 
                                    padding: "8px 16px",
                                    background: "#ffffff",
                                    borderRadius: 8,
                                    border: "1px solid #e2e8f0",
                                    display: "flex",
                                    alignItems: "center",
                                    gap: 8,
                                    cursor: "default"
                                  }}
                                >
                                  <motion.span 
                                    initial={{ scale: 0 }}
                                    animate={{ scale: 1 }}
                                    transition={{
                                      duration: 0.3,
                                      delay: 0.3 + (i * 0.1),
                                      type: "spring",
                                      stiffness: 200
                                    }}
                                    style={{ color: "#7c3aed", fontSize: "14px" }}
                                  >
                                    •
                                  </motion.span>
                                  {detail}
                                </motion.li>
                              ))}
                            </ul>
                            <motion.button
                              onClick={() => handleShowConfirmDialog(pkg)}
                              initial={{
                                opacity: 0,
                                y: 20,
                                scale: 0.9
                              }}
                              animate={{
                                opacity: 1,
                                y: 0,
                                scale: 1
                              }}
                              transition={{
                                duration: 0.4,
                                delay: 0.4 + (pkg.details.length * 0.1),
                                ease: [0.25, 0.46, 0.45, 0.94]
                              }}
                              whileHover={{
                                scale: 1.02,
                                y: -2,
                                boxShadow: "0 8px 25px rgba(124, 58, 237, 0.3)"
                              }}
                              whileTap={{
                                scale: 0.98,
                                y: 0
                              }}
                              style={{
                                marginTop: 20,
                                width: "100%",
                                background: "#7c3aed",
                                color: "#fff",
                                border: "none",
                                borderRadius: 8,
                                padding: "12px 0",
                                fontWeight: 600,
                                fontSize: 14,
                                cursor: "pointer",
                                boxShadow: "0 4px 15px rgba(124, 58, 237, 0.2)"
                              }}
                            >
                              🛒 Paketi Seç
                            </motion.button>
                          </motion.div>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Confirmation Dialog */}
      {showConfirmDialog && (
        <div style={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: "rgba(0,0,0,0.6)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          zIndex: 9999,
          padding: "16px",
          animation: "fadeIn 0.3s ease"
        }}>
          <div style={{
            background: "#ffffff",
            borderRadius: "20px",
            padding: isDesktop ? "32px" : "24px",
            maxWidth: isDesktop ? "500px" : "400px",
            width: "100%",
            boxShadow: "0 20px 40px rgba(0,0,0,0.2)",
            animation: "slideInUp 0.4s ease",
            position: "relative"
          }}>
            {/* Header */}
            <div style={{
              textAlign: "center",
              marginBottom: "24px"
            }}>
              <div style={{
                fontSize: isDesktop ? "48px" : "40px",
                marginBottom: "8px"
              }}>
                🛒
              </div>
              <h3 style={{
                fontSize: isDesktop ? "24px" : "20px",
                fontWeight: "700",
                color: "#1f2937",
                margin: 0,
                marginBottom: "8px"
              }}>
                Paket Satın Al
              </h3>
              <p style={{
                fontSize: isDesktop ? "16px" : "14px",
                color: "#6b7280",
                margin: 0
              }}>
                Bu paketi satın almak istediğinizden emin misiniz?
              </p>
            </div>

            {/* Package Info */}
            {selectedPackageForPurchase && (
              <div style={{
                background: "linear-gradient(135deg, #f8fafc, #e2e8f0)",
                borderRadius: "16px",
                padding: isDesktop ? "20px" : "16px",
                marginBottom: "24px",
                border: "1px solid #e2e8f0"
              }}>
                <div style={{
                  fontSize: isDesktop ? "20px" : "18px",
                  fontWeight: "700",
                  color: "#1f2937",
                  marginBottom: "8px",
                  textAlign: "center"
                }}>
                  📦 {selectedPackageForPurchase.name}
                </div>
                <div style={{
                  fontSize: isDesktop ? "16px" : "14px",
                  color: "#475569",
                  textAlign: "center",
                  marginBottom: "12px"
                }}>
                  {selectedPackageForPurchase.summary}
                </div>
                <div style={{
                  fontSize: isDesktop ? "24px" : "20px",
                  fontWeight: "800",
                  color: "#059669",
                  textAlign: "center"
                }}>
                  {selectedPackageForPurchase.price} TL
                </div>
              </div>
            )}

            {/* Buttons */}
            <div style={{
              display: "flex",
              gap: "12px",
              justifyContent: "center"
            }}>
              <button
                onClick={handleCancelPurchase}
                disabled={isPurchasing}
                style={{
                  flex: 1,
                  padding: isDesktop ? "14px 20px" : "12px 16px",
                  border: "2px solid #e5e7eb",
                  borderRadius: "12px",
                  background: "#ffffff",
                  color: "#6b7280",
                  fontSize: isDesktop ? "16px" : "14px",
                  fontWeight: "600",
                  cursor: isPurchasing ? "not-allowed" : "pointer",
                  transition: "all 0.3s ease",
                  opacity: isPurchasing ? 0.5 : 1
                }}
                onMouseOver={e => {
                  if (!isPurchasing) {
                    e.target.style.borderColor = "#d1d5db";
                    e.target.style.background = "#f9fafb";
                  }
                }}
                onMouseOut={e => {
                  if (!isPurchasing) {
                    e.target.style.borderColor = "#e5e7eb";
                    e.target.style.background = "#ffffff";
                  }
                }}
              >
                ❌ İptal
              </button>
              
              <button
                onClick={handleConfirmPurchase}
                disabled={isPurchasing}
                style={{
                  flex: 1,
                  padding: isDesktop ? "14px 20px" : "12px 16px",
                  border: "none",
                  borderRadius: "12px",
                  background: isPurchasing ? "#9ca3af" : "#7c3aed",
                  color: "#ffffff",
                  fontSize: isDesktop ? "16px" : "14px",
                  fontWeight: "600",
                  cursor: isPurchasing ? "not-allowed" : "pointer",
                  transition: "all 0.3s ease",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "8px"
                }}
                onMouseOver={e => {
                  if (!isPurchasing) {
                    e.target.style.background = "#6d28d9";
                  }
                }}
                onMouseOut={e => {
                  if (!isPurchasing) {
                    e.target.style.background = "#7c3aed";
                  }
                }}
              >
                {isPurchasing && (
                  <div style={{
                    width: "16px",
                    height: "16px",
                    border: "2px solid rgba(255,255,255,0.3)",
                    borderTop: "2px solid #fff",
                    borderRadius: "50%",
                    animation: "spin 1s linear infinite"
                  }}></div>
                )}
                {isPurchasing ? "Satın Alınıyor..." : "✅ Onayla"}
              </button>
            </div>
          </div>
        </div>
      )}

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
        
        @keyframes slideInUp {
          from {
            opacity: 0;
            transform: translateY(30px) scale(0.95);
          }
          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
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