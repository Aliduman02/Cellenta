import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { CheckCircle, FileText, ChevronRight } from "lucide-react";
import AppleStyleDock from "./components/AppleStyleDock";
import Sidebar from "./components/Sidebar";
import ChatWidget from "./components/ChatWidget";



import apiService from "./services/api";

export default function Bills() {
  const [bills, setBills] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
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
    const loadBills = async () => {
      try {
        setIsLoading(true);
        const billsData = await apiService.getBills();
        console.log('Bills API response:', billsData);
        // API'den gelen faturaları uygun şekilde map'le
        const mappedBills = (billsData || []).map(bill => {
          console.log('Bill payment status:', bill.paymentStatus, 'for bill ID:', bill.id);
          
          // API'den gelen farklı status değerlerini kontrol et
          let status = 'Bilinmiyor';
          if (bill.paymentStatus === 'paid' || bill.paymentStatus === 'ödendi' || bill.paymentStatus === 'PAID') {
            status = 'Ödendi';
          } else if (bill.paymentStatus === 'unpaid' || bill.paymentStatus === 'ödenmemiş' || bill.paymentStatus === 'UNPAID') {
            status = 'Ödenmedi';
          } else if (bill.paymentStatus === 'overdue' || bill.paymentStatus === 'vadesi_geçti' || bill.paymentStatus === 'OVERDUE') {
            status = 'Vadesi Geçti';
          } else {
            // Bilinmeyen durum için gerçek değeri göster
            status = `Durum: ${bill.paymentStatus}`;
          }
          
          return {
            id: bill.id,
            date: bill.startDate ? bill.startDate.split('T')[0] : '',
            amount: (bill.price !== undefined ? bill.price + ' TL' : ''),
            status: status,
            left: bill.daysLeft // API'de yok, isterseniz hesaplanabilir
          };
        });
        setBills(mappedBills);
      } catch (error) {
        console.error('Failed to load bills:', error);
        setBills([]);
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          apiService.removeAuthToken();
          window.location.href = '/login';
        }
      } finally {
        setIsLoading(false);
      }
    };

    loadBills();
  }, []);

  const handlePayBill = async (billId) => {
    try {
      await apiService.payBill(billId);
      // Bills'i yeniden yükle
      const updatedBills = await apiService.getBills();
      setBills(updatedBills);
    } catch (error) {
      console.error('Failed to pay bill:', error);
      alert('Ödeme başarısız. Lütfen tekrar deneyin.');
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
            Faturalar yükleniyor...
          </div>
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
            💳 Son Ödemeler
          </span>
        </div>

        {/* Bills Container */}
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
          {/* Ödenmemiş fatura kutusu (ilk sırada, kartların içinde) */}
          {bills.length > 0 && bills.find(bill => bill.status === "Ödenmedi") && (
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5 }}
              className="pending-bill-container"
              style={{
                border: "2px solid #fbbf24",
                borderRadius: 20,
                marginBottom: 24,
                background: "linear-gradient(135deg, #fffbeb, #fef3c7)",
                boxShadow: "0 4px 20px rgba(251, 191, 36, 0.15)",
                transition: "all 0.3s ease",
                overflow: "hidden",
                display: "flex",
                alignItems: "center",
                padding: "20px 28px",
                gap: 20,
                minHeight: 80,
                maxWidth: "100%",
                justifyContent: "space-between",
                position: "relative"
              }}
            >
              {/* Glow effect */}
              <div 
                style={{
                  position: "absolute",
                  top: -2,
                  left: -2,
                  right: -2,
                  bottom: -2,
                  background: "linear-gradient(45deg, rgba(251,191,36,0.3), rgba(245,158,11,0.3))",
                  borderRadius: 22,
                  zIndex: -1,
                  animation: "pulse 2s infinite"
                }}
              />
              
              <div className="pending-bill-info" style={{
                display: "flex",
                flexDirection: "column",
                gap: 8,
                flex: 1
              }}>
                <div style={{
                  fontSize: 18,
                  fontWeight: 700,
                  color: "#dc2626",
                  display: "flex",
                  alignItems: "center",
                  gap: 8
                }}>
                  ⚠️ Bekleyen Fatura
                </div>
                <div style={{
                  fontSize: 14,
                  color: "#6b7280",
                  fontWeight: 500,
                  lineHeight: 1.4
                }}>
                  Ödenmemiş faturanız bulunmaktadır. Lütfen en kısa sürede ödeme yapınız.
                </div>
                <div
                  style={{ 
                    background: "linear-gradient(135deg, #9ca3af, #6b7280)", 
                    color: "#fff", 
                    border: "none", 
                    borderRadius: 16, 
                    padding: "8px 16px", 
                    fontWeight: 600, 
                    fontSize: 13, 
                    cursor: "not-allowed",
                    boxShadow: "0 2px 8px rgba(107,114,128,0.2)",
                    transition: "all 0.3s ease",
                    display: "flex",
                    alignItems: "center",
                    gap: 6,
                    opacity: 0.6,
                    alignSelf: "flex-start"
                  }}
                >
                  💳 Faturayı Öde <ChevronRight size={16} />
                </div>
              </div>
              
              <div className="pending-bill-amount" style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "flex-end",
                gap: 4
              }}>
                <div style={{ 
                  fontWeight: 800, 
                  fontSize: 24, 
                  color: "#dc2626",
                  textShadow: "0 2px 4px rgba(0,0,0,0.1)"
                }}>
                  {bills.find(bill => bill.status === "Ödenmedi")?.amount}
                </div>
                <div style={{
                  fontSize: 14,
                  color: "#6b7280",
                  fontWeight: 500
                }}>
                  📅 {bills.find(bill => bill.status === "Ödenmedi")?.date}
                </div>
              </div>
            </motion.div>
          )}

          {/* Bills List */}
          {bills.length === 0 ? (
            <div style={{ 
              textAlign: "center", 
              padding: "48px 24px",
              color: "#6b7280",
              fontSize: "16px",
              animation: "fadeIn 1s ease"
            }}>
              📄 Fatura bulunamadı
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
              {bills.map((bill, i) => (
                <motion.div
                  key={bill.id}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.5, delay: i * 0.1 }}
                  style={{
                    border: "2px solid #e5e7eb",
                    borderRadius: 20,
                    background: "rgba(255,255,255,0.9)",
                    boxShadow: "0 4px 16px rgba(0,0,0,0.08)",
                    transition: "all 0.3s ease",
                    overflow: "hidden",
                    display: "flex",
                    alignItems: "center",
                    padding: "20px 28px",
                    gap: 20,
                    justifyContent: "space-between"
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = "translateY(-2px)";
                    e.currentTarget.style.boxShadow = "0 6px 24px rgba(0,0,0,0.12)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = "translateY(0)";
                    e.currentTarget.style.boxShadow = "0 4px 16px rgba(0,0,0,0.08)";
                  }}
                >
                  <div style={{ 
                    width: 44, 
                    height: 44, 
                    display: "flex", 
                    alignItems: "center", 
                    justifyContent: "center", 
                    borderRadius: "50%", 
                    background: bill.status === "Ödendi" 
                      ? "linear-gradient(135deg, #d1fae5, #a7f3d0)" // Yeşil
                      : bill.status === "Ödenmedi"
                      ? "linear-gradient(135deg, #fee2e2, #fecaca)" // Kırmızı
                      : "linear-gradient(135deg, #fef3c7, #fde68a)", // Sarı (Vadesi Geçti)
                    boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                  }}>
                    {bill.status === "Ödendi" ? (
                      <CheckCircle size={24} color="#059669" />
                    ) : bill.status === "Ödenmedi" ? (
                      <FileText size={24} color="#dc2626" />
                    ) : (
                      <FileText size={24} color="#d97706" />
                    )}
                  </div>
                  
                  <div style={{ flex: 1 }}>
                    <div style={{ 
                      fontWeight: 700, 
                      fontSize: 18, 
                      color: "#1f2937",
                      marginBottom: 4
                    }}>
                      📅 {bill.date}
                    </div>
                    <div style={{ 
                      fontSize: 15, 
                      color: "#6b7280", 
                      fontWeight: 500
                    }}>
                                                Tutar: {bill.amount}
                    </div>
                  </div>
                  
                  <div style={{ 
                    minWidth: 100, 
                    textAlign: "right", 
                    color: bill.status === "Ödendi" 
                      ? "#059669" // Yeşil
                      : bill.status === "Ödenmedi"
                      ? "#dc2626" // Kırmızı
                      : "#d97706", // Sarı (Vadesi Geçti)
                    fontWeight: 700, 
                    fontSize: 16,
                    padding: "8px 16px",
                    borderRadius: 12,
                    background: bill.status === "Ödendi" 
                      ? "rgba(5, 150, 105, 0.1)" // Yeşil arkaplan
                      : bill.status === "Ödenmedi"
                      ? "rgba(220, 38, 38, 0.1)" // Kırmızı arkaplan
                      : "rgba(217, 119, 6, 0.1)", // Sarı arkaplan (Vadesi Geçti)
                    border: `2px solid ${bill.status === "Ödendi" 
                      ? "rgba(5, 150, 105, 0.2)" 
                      : bill.status === "Ödenmedi"
                      ? "rgba(220, 38, 38, 0.2)"
                      : "rgba(217, 119, 6, 0.2)"}`
                  }}>
                    {bill.status === "Ödendi" ? `✅ ${bill.status}` : bill.status === "Ödenmedi" ? `❌ ${bill.status}` : `⏳ ${bill.status}`}
                  </div>
                </motion.div>
              ))}
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
          .bills-container {
            margin: 0 16px;
            padding: 24px 20px !important;
          }
          
          .bill-item {
            flex-direction: column;
            align-items: flex-start !important;
            gap: 12px !important;
            padding: 16px 20px !important;
          }
          
          .bill-status {
            align-self: flex-end;
            min-width: auto !important;
          }
          
          /* Özel fatura kutusu mobil responsive */
          .pending-bill-container {
            flex-direction: column !important;
            gap: 16px !important;
            padding: 20px !important;
            text-align: center !important;
          }
          
          .pending-bill-info {
            align-items: center !important;
          }
          
          .pending-bill-amount {
            align-items: center !important;
            text-align: center !important;
          }
        }
      `}</style>
    </div>
  );
} 