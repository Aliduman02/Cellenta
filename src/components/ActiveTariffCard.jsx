import React from "react";

export default function ActiveTariffCard({ name, price }) {
  return (
    <div className="active-tariff-card">
      <div className="active-tariff-title">Current Package</div>
      <div className="active-tariff-name">{name || 'No Package'}</div>
      <div className="active-tariff-price">
        {price ? `${price} TL` : 'No active package'}
        {price && <span className="per-month">/month</span>}
      </div>
    </div>
  );
}
