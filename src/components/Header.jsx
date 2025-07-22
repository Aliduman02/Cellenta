import ActiveTariffCard from "./ActiveTariffCard";

export default function Header({ user, activeTariff }) {
  // API'den gelen veriyi component formatına çevir
  const formattedUser = {
    name: user?.name || user?.firstName || 'User',
    phone: user?.msisdn || user?.phone || 'N/A'
  };

  return (
    <header className="dashboard-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 0 }}>
      <div className="header-section header-userinfo" style={{ background: '#f3f6fb', borderRadius: 20, padding: '28px 40px', width: 280, boxShadow: '0 2px 16px rgba(0,0,0,0.04)', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
        <div className="header-username" style={{ fontSize: 24, fontWeight: 700, marginBottom: 8 }}>Hello, {formattedUser.name}</div>
        <div className="header-phone" style={{ fontSize: 18, color: '#555' }}>{formattedUser.phone}</div>
      </div>
      <div className="header-section header-logo" style={{ width: 200, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <img src="/images/title2.png" alt="Cellenta" height={40} />
      </div>
      <div className="header-section header-tariff" style={{ width: 360, display: 'flex', justifyContent: 'flex-end' }}>
        <ActiveTariffCard name={activeTariff?.name || ''} price={activeTariff?.price || ''} />
      </div>
    </header>
  );
}
