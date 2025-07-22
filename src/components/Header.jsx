import ActiveTariffCard from "./ActiveTariffCard";

export default function Header({ user, activeTariff }) {
  // API'den gelen veriyi component formatına çevir
  const formattedUser = {
    name: user?.name || user?.firstName || 'User',
    phone: user?.msisdn || user?.phone || 'N/A'
  };

  return (
    <header className="dashboard-header">
      <div className="header-section header-userinfo">
        <div className="header-username">Hello, {formattedUser.name}</div>
        <div className="header-phone">{formattedUser.phone}</div>
      </div>
      
      <div className="header-section header-logo">
        <img src="/images/title2.png" alt="Cellenta" />
      </div>
      
      <div className="header-section header-tariff">
        <ActiveTariffCard name={activeTariff?.name || ''} price={activeTariff?.price || ''} />
      </div>
    </header>
  );
}
