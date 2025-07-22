import React from "react";

export default function ActiveTariffCard({ name, price }) {
  return (
    <div className="active-tariff-card" style={{
      background: '#f3f6fb',
      borderRadius: 20,
      padding: '28px 40px',
      width: 280,
      boxShadow: '0 2px 16px rgba(0,0,0,0.04)',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: 8,
      textAlign: 'center'
    }}>
      <div style={{ fontSize: 24, fontWeight: 700, marginBottom: 8 }}>{name || 'No Package'}</div>
      <div style={{ fontSize: 18, color: '#555' }}>{price ? `${price} TL/month` : 'No active package'}</div>
    </div>
  );
}
