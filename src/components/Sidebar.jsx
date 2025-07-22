import React from "react";

export default function Sidebar({ user }) {
  const currentPath = window.location.pathname;
  return (
    <aside className="sidebar">
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
  );
}
