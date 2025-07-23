import React from "react";

export default function ActiveTariffCard({ name, price }) {
  return (
    <div className="active-tariff-card">
      <div className="active-tariff-title">Güncel Paket</div>
      <div className="active-tariff-name">{name || 'Paket Yok'}</div>
      <div className="active-tariff-price">
        {price ? `${price} TL` : 'Aktif paket yok'}
        {price && <span className="per-month">/ay</span>}
      </div>
    </div>
  );
}
