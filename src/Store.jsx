import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronRight } from "lucide-react";
import AppleStyleDock from "./components/AppleStyleDock";
import ChatWidget from "./components/ChatWidget";
import apiService from "./services/api";

export default function Store() {
  const [packages, setPackages] = useState([]);
  const [openId, setOpenId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

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
        setError('Failed to load packages. Please try again.');
        
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
      alert('Package purchased successfully!');
      window.location.href = '/dashboard';
    } catch (error) {
      console.error('Failed to purchase package:', error);
      alert('Purchase failed. Please try again.');
    }
  };

  if (isLoading) {
    return (
      <div
        style={{
          minHeight: "100vh",
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
            Loading packages...
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
          <span style={{ fontSize: 28, fontWeight: 700, color: "#222" }}>Store</span>
        </div>
        <div style={{ background: "rgba(255,255,255,0.7)", borderRadius: 24, boxShadow: "0 2px 16px rgba(0,0,0,0.06)", padding: 24 }}>
          {packages.length === 0 ? (
            <div style={{ 
              textAlign: "center", 
              padding: "48px 24px",
              color: "#6b7280",
              fontSize: "16px"
            }}>
              No packages available
            </div>
          ) : (
            packages.map((pkg) => {
              const isOpen = openId === pkg.id;
              return (
                <div
                  key={pkg.id}
                  style={{
                    border: "1px solid #e5e7eb",
                    borderRadius: 16,
                    marginBottom: 18,
                    background: isOpen ? "#f5f3ff" : "#fff",
                    boxShadow: isOpen ? "0 4px 16px rgba(124,60,237,0.10)" : "0 1px 4px rgba(0,0,0,0.03)",
                    transition: "all 0.3s",
                    overflow: "hidden",
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
                      padding: "20px 24px",
                      cursor: "pointer",
                      fontSize: 18,
                      fontWeight: 700,
                      color: isOpen ? "#7c3aed" : "#222",
                      outline: "none",
                    }}
                  >
                    <div style={{ textAlign: "left" }}>
                      <div style={{ fontWeight: 700 }}>{pkg.name}</div>
                      <div style={{ fontWeight: 400, fontSize: 15, color: "#444", marginTop: 2 }}>{pkg.summary}</div>
                    </div>
                    <motion.span
                      animate={{ rotate: isOpen ? 90 : 0 }}
                      transition={{ duration: 0.3 }}
                      style={{ display: "flex", alignItems: "center", justifyContent: "center", background: "#ede9fe", borderRadius: "50%", width: 36, height: 36 }}
                    >
                      <ChevronRight size={22} color="#7c3aed" />
                    </motion.span>
                  </button>
                  <AnimatePresence initial={false}>
                    {isOpen && (
                      <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.4, ease: "easeInOut" }}
                        style={{ overflow: "hidden", background: "#fafaff", padding: "0 24px 20px 24px" }}
                      >
                        <div style={{ fontWeight: 700, fontSize: 18, margin: "18px 0 8px 0", color: "#222" }}>{pkg.name}</div>
                        <ul style={{ listStyle: "none", padding: 0, margin: 0, fontSize: 16, color: "#444" }}>
                          {pkg.details.map((d, i) => (
                            <li key={i} style={{ marginBottom: 4 }}>{d}</li>
                          ))}
                        </ul>
                        <button
                          onClick={() => handlePurchasePackage(pkg.id)}
                          style={{
                            marginTop: 18,
                            width: "100%",
                            background: "#ede9fe",
                            color: "#7c3aed",
                            border: "none",
                            borderRadius: 8,
                            padding: "12px 0",
                            fontWeight: 600,
                            fontSize: 16,
                            cursor: "pointer",
                            transition: "background 0.2s",
                          }}
                        >
                          Select Package
                        </button>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              );
            })
          )}
        </div>
      </div>
      <ChatWidget />
    </div>
  );
} 