import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { CheckCircle, FileText, ChevronRight } from "lucide-react";
import AppleStyleDock from "./components/AppleStyleDock";
import ChatWidget from "./components/ChatWidget";
import apiService from "./services/api";

export default function Bills() {
  const [bills, setBills] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadBills = async () => {
      try {
        setIsLoading(true);
        const billsData = await apiService.getBills();
        console.log('Bills API response:', billsData);
        // API'den gelen faturaları uygun şekilde map'le
        const mappedBills = (billsData || []).map(bill => ({
          id: bill.id,
          date: bill.startDate ? bill.startDate.split('T')[0] : '',
          amount: (bill.price !== undefined ? bill.price + ' TL' : ''),
          status: bill.paymentStatus === 'PAID' ? 'Paid' : bill.paymentStatus === 'UNPAID' ? 'Unpaid' : bill.paymentStatus,
          left: bill.daysLeft // API'de yok, isterseniz hesaplanabilir
        }));
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
      alert('Payment failed. Please try again.');
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
            Loading bills...
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
      }}
    >
      <AppleStyleDock />
      <div style={{ maxWidth: 900, margin: "0 auto", padding: "48px 0 0 0" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 32 }}>
          <div className="header-logo" style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
            <img src="/images/title2.png" alt="Cellenta" height={48} style={{ height: 48, width: "auto", objectFit: "contain" }} />
          </div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 32 }}>
          <span style={{ fontSize: 28, fontWeight: 700, color: "#222" }}>Last Payments</span>
        </div>
        <div style={{ background: "rgba(255,255,255,0.7)", borderRadius: 24, boxShadow: "0 2px 16px rgba(0,0,0,0.06)", padding: 24 }}>
          {/* Ödenmemiş fatura kutusu (ilk sırada, kartların içinde) */}
          {bills.length > 0 && bills[0].status === "unpaid" && (
            <div
              style={{
                border: "1px solid #e5e7eb",
                borderRadius: 16,
                marginBottom: 18,
                background: "#f5faff",
                boxShadow: "0 1px 4px rgba(0,0,0,0.03)",
                transition: "all 0.3s",
                overflow: "hidden",
                display: "flex",
                alignItems: "center",
                padding: "18px 24px",
                gap: 18,
                minHeight: 72,
                maxWidth: "100%",
                justifyContent: "space-between",
              }}
            >
              <button 
                onClick={() => handlePayBill(bills[0].id)}
                style={{ 
                  background: "#ede9fe", 
                  color: "#7c3aed", 
                  border: "none", 
                  borderRadius: 24, 
                  padding: "10px 18px", 
                  fontWeight: 600, 
                  fontSize: 15, 
                  cursor: "pointer" 
                }}
              >
                Pay the bill <ChevronRight size={18} style={{ verticalAlign: "middle" }} />
              </button>
              <div style={{ fontWeight: 700, fontSize: 24, color: "#222" }}>{bills[0].amount}</div>
            </div>
          )}
          {bills.map((bill, i) => (
            <div
              key={bill.id}
              style={{
                border: "1px solid #e5e7eb",
                borderRadius: 16,
                marginBottom: 18,
                background: "#fff",
                boxShadow: "0 1px 4px rgba(0,0,0,0.03)",
                transition: "all 0.3s",
                overflow: "hidden",
                display: "flex",
                alignItems: "center",
                padding: "18px 24px",
                gap: 18,
                justifyContent: "space-between"
              }}
            >
              <div style={{ width: 36, height: 36, display: "flex", alignItems: "center", justifyContent: "center", borderRadius: "50%", background: bill.status === "Paid" ? "#d1fae5" : "#fef3c7" }}>
                {bill.status === "Paid" ? (
                  <CheckCircle size={22} color="#10b981" />
                ) : (
                  <FileText size={22} color="#f59e42" />
                )}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600, fontSize: 17, color: "#222" }}>{bill.date}</div>
                <div style={{ fontSize: 15, color: "#666", marginTop: 2 }}>Amount: {bill.amount}</div>
              </div>
              <div style={{ minWidth: 90, textAlign: "right", color: bill.status === "Paid" ? "#10b981" : "#f59e42", fontWeight: 600, fontSize: 15 }}>
                {bill.status === "Paid" ? "Paid" : "Unpaid"}
              </div>
            </div>
          ))}
          {bills.length === 0 && (
            <div style={{ 
              textAlign: "center", 
              padding: "48px 24px",
              color: "#6b7280",
              fontSize: "16px"
            }}>
              No bills found
            </div>
          )}
        </div>
      </div>
      <ChatWidget />
    </div>
  );
} 