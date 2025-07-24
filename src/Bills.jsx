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
  const [openBillId, setOpenBillId] = useState(null);

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
        if (Array.isArray(billsData) && billsData.length > 0) {
          console.log('Örnek fatura nesnesi:', billsData[0]);
        }
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
            left: bill.daysLeft, // API'de yok, isterseniz hesaplanabilir
            startDate: bill.startDate || bill.sdate || '',
            endDate: bill.endDate || bill.edate || '',
            dueDate: bill.dueDate || bill.lastDate || bill.due_date || bill.last_date || '',
            isActive: bill.isActive === true || bill.isActive === 'true' || bill.isActive === 1 || bill.isActive === '1',
            price: bill.price,
            paymentStatus: bill.paymentStatus
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

  // Tüm faturalar gösterilecek
  // Aktif fatura
  const activeBill = bills.find(bill => bill.isActive);

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
        {/* Aktif Fatura Kutusu */}
        {activeBill && (
          <div style={{
            border: "2px solid #6366f1",
            background: "linear-gradient(90deg, #f0f9ff 60%, #a5b4fc 100%)",
            borderRadius: 20,
            boxShadow: "0 6px 24px rgba(99,102,241,0.10)",
            padding: isDesktop ? "28px 32px" : "20px 16px",
            marginBottom: 32,
            display: "flex",
            flexDirection: isDesktop ? "row" : "column",
            alignItems: isDesktop ? "center" : "flex-start",
            gap: isDesktop ? 32 : 12,
            position: "relative"
          }}>
            <div style={{
              fontSize: 22,
              fontWeight: 800,
              color: "#3730a3",
              marginBottom: isDesktop ? 0 : 8,
              letterSpacing: "-0.5px"
            }}>
              💡 Aktif Fatura
            </div>
            <div style={{ display: "flex", flexDirection: isDesktop ? "row" : "column", gap: isDesktop ? 32 : 6, flexWrap: "wrap" }}>
              {activeBill.startDate && (
                <div style={{ color: "#3730a3", fontWeight: 600 }}>
                  Başlangıç: {activeBill.startDate.split('T')[0]}
                </div>
              )}
              {activeBill.endDate && (
                <div style={{ color: "#3730a3", fontWeight: 600 }}>
                  Bitiş: {activeBill.endDate.split('T')[0]}
                </div>
              )}
              <div style={{ color: "#059669", fontWeight: 700 }}>
                Tutar: {activeBill.price} TL
              </div>
              <div style={{ color: activeBill.paymentStatus === 'PAID' ? "#059669" : "#dc2626", fontWeight: 700 }}>
                Durum: {activeBill.paymentStatus === 'PAID' ? 'Ödendi' : activeBill.paymentStatus === 'UNPAID' ? 'Ödenmedi' : activeBill.paymentStatus}
              </div>
            </div>
          </div>
        )}

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
               {bills.map((bill, i) => {
                 const isOpen = openBillId === bill.id;
                 return (
                   <div key={bill.id}>
                     <motion.div
                       initial={{ opacity: 0, x: -20 }}
                       animate={{ opacity: 1, x: 0 }}
                       transition={{ duration: 0.5, delay: i * 0.1 }}
                       style={{
                         border: "2px solid #e5e7eb",
                         borderRadius: 20,
                         background: bill.status === "Ödenmedi"
                           ? "rgba(255,255,255,0.97)"
                           : "rgba(243,244,246,0.97)",
                         boxShadow: "0 4px 16px rgba(0,0,0,0.08)",
                         transition: "all 0.3s ease",
                         overflow: "hidden",
                         display: "flex",
                         alignItems: "center",
                         padding: "20px 28px",
                         gap: 20,
                         justifyContent: "space-between",
                         cursor: "pointer"
                       }}
                       onClick={() => setOpenBillId(isOpen ? null : bill.id)}
                       onMouseEnter={e => {
                         e.currentTarget.style.transform = "translateY(-2px)";
                         e.currentTarget.style.boxShadow = "0 6px 24px rgba(0,0,0,0.12)";
                       }}
                       onMouseLeave={e => {
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
                         background: bill.status === "Ödenmedi"
                           ? "linear-gradient(135deg, #fee2e2, #fecaca)"
                           : "linear-gradient(135deg, #d1fae5, #a7f3d0)",
                         boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                       }}>
                         {bill.status === "Ödenmedi" ? (
                           <FileText size={24} color="#dc2626" />
                         ) : (
                           <CheckCircle size={24} color="#059669" />
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
                         color: bill.status === "Ödenmedi" ? "#dc2626" : "#059669",
                         fontWeight: 700, 
                         fontSize: 16,
                         padding: "8px 16px",
                         borderRadius: 12,
                         background: bill.status === "Ödenmedi"
                           ? "rgba(220, 38, 38, 0.1)"
                           : "rgba(5, 150, 105, 0.1)",
                         border: bill.status === "Ödenmedi"
                           ? "2px solid rgba(220, 38, 38, 0.2)"
                           : "2px solid rgba(5, 150, 105, 0.2)"
                       }}>
                         {bill.status === "Ödenmedi" ? `❌ ${bill.status}` : `✅ ${bill.status}`}
                       </div>
                     </motion.div>
                     {/* Akordiyon detay */}
                     {isOpen && (
                       <div style={{
                         background: "#f3f4f6",
                         borderTop: "1px solid #e5e7eb",
                         padding: "18px 28px",
                         color: "#1f2937",
                         fontSize: 15,
                         borderRadius: "0 0 20px 20px"
                       }}>
                         {bill.startDate && (
                           <div style={{ marginBottom: 4, color: "#3730a3" }}>
                             Başlangıç Tarihi: {bill.startDate.split('T')[0]}
                           </div>
                         )}
                         {bill.endDate && (
                           <div style={{ marginBottom: 4, color: "#3730a3" }}>
                             Bitiş Tarihi: {bill.endDate.split('T')[0]}
                           </div>
                         )}
                         {bill.dueDate && (
                           <div style={{ marginBottom: 4, color: "#b91c1c" }}>
                             Son Ödeme Tarihi: {bill.dueDate}
                           </div>
                         )}
                         <div style={{ marginTop: 8, color: bill.status === "Ödenmedi" ? "#dc2626" : "#059669", fontWeight: 700 }}>
                           Durum: {bill.status}
                         </div>
                       </div>
                     )}
                   </div>
                 );
               })}
             </div>
           )}

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