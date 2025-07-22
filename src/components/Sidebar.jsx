import React, { useState } from "react";

export default function Sidebar({ user }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const currentPath = window.location.pathname;
  const isMobile = window.innerWidth <= 900;

  return (
    <>
      {isMobile && (
        <button className="hamburger-btn" onClick={() => setMenuOpen(true)}>
          ☰
        </button>
      )}
      <aside className={`sidebar${isMobile ? ' sidebar-drawer' : ''}${menuOpen ? ' open' : ''}`} style={isMobile ? { display: menuOpen ? 'flex' : 'none' } : {}}>
        <button className="close-btn" style={{ display: isMobile ? 'block' : 'none' }} onClick={() => setMenuOpen(false)}>×</button>
        <div className="sidebar-logo">
          <img src="/images/icon2.png" alt="Cellenta Logo" style={{ height: 48 }} />
          <img src="/images/title2.png" alt="Cellenta" style={{ height: 32, marginTop: 8 }} />
        </div>
        <nav className="sidebar-menu">
          <a href="/dashboard" className={currentPath === "/dashboard" ? "active" : ""}>HOME</a>
          <a href="/store" className={currentPath === "/store" ? "active" : ""}>STORE</a>
          <a href="/bills" className={currentPath === "/bills" ? "active" : ""}>BILLS</a>
          <a href="/profile" className={currentPath === "/profile" ? "active" : ""}>PROFILE</a>
        </nav>
      </aside>
      {isMobile && menuOpen && <div className="drawer-backdrop" onClick={() => setMenuOpen(false)} />}
    </>
  );
}
