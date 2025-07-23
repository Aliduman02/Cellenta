import React, { useState, useEffect } from "react";
import { Home, Package, FileText, User, ChevronLeft, ChevronRight } from "lucide-react";

export default function Sidebar({ user }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const currentPath = window.location.pathname;

  // Responsive check
  useEffect(() => {
    const checkScreenSize = () => {
      const mobile = window.innerWidth <= 1199; // Tablet'i de mobile olarak say
      setIsMobile(mobile);
      
      // Desktop'ta sidebar açık, diğer tüm cihazlarda hamburger menü
      if (window.innerWidth >= 1200) {
        setIsCollapsed(false);
      } else {
        setIsCollapsed(true);
      }
    };

    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

  const menuItems = [
    { href: "/dashboard", icon: <Home />, label: "Ana Sayfa" },
    { href: "/store", icon: <Package />, label: "Mağaza" },
    { href: "/bills", icon: <FileText />, label: "Faturalar" },
    { href: "/profile", icon: <User />, label: "Profil" },
  ];

  const toggleSidebar = () => {
    if (isMobile) {
      setMenuOpen(!menuOpen);
    } else {
      setIsCollapsed(!isCollapsed);
    }
  };

  const closeMobileSidebar = () => {
    if (isMobile) {
      setMenuOpen(false);
    }
  };

  // Close mobile menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (isMobile && menuOpen && !event.target.closest('.sidebar') && !event.target.closest('.hamburger-btn')) {
        setMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isMobile, menuOpen]);

  return (
    <>
      {/* Hamburger button for mobile */}
      {isMobile && (
        <button className="hamburger-btn" onClick={toggleSidebar}>
          ☰
        </button>
      )}

      {/* Backdrop for mobile */}
      {isMobile && menuOpen && (
        <div 
          className={`sidebar-backdrop ${menuOpen ? 'active' : ''}`} 
          onClick={closeMobileSidebar}
        />
      )}

      {/* Sidebar */}
      <aside 
        className={`sidebar ${isMobile && menuOpen ? 'open' : ''} ${!isMobile && isCollapsed ? 'collapsed' : ''}`}
      >
        {/* Collapse button for desktop */}
        {!isMobile && (
          <button 
            className="sidebar-collapse-btn" 
            onClick={toggleSidebar}
            title={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
          >
            {isCollapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
          </button>
        )}

        {/* Close button for mobile */}
        {isMobile && (
          <button className="close-btn" onClick={closeMobileSidebar}>
            ×
          </button>
        )}

        {/* Logo */}
        <div className="sidebar-logo">
          <img src="/images/icon2.png" alt="Cellenta Logo" style={{ height: isCollapsed ? 32 : 48 }} />
          {!isCollapsed && (
            <img src="/images/title2.png" alt="Cellenta" style={{ height: 32, marginTop: 8 }} />
          )}
        </div>

        {/* User info */}
        {user && (!isCollapsed || isMobile) && (
          <div className="sidebar-user">
            <div className="sidebar-username">{user.name || user.firstName}</div>
            <div className="sidebar-phone">{user.phone || user.msisdn}</div>
          </div>
        )}

        {/* Navigation menu */}
        <nav className="sidebar-menu">
          {menuItems.map(item => (
            <a 
              key={item.href} 
              href={item.href} 
              className={currentPath === item.href ? "active" : ""}
              onClick={closeMobileSidebar}
              title={isCollapsed ? item.label : ""}
            >
              <span className="sidebar-icon">{item.icon}</span>
              {(!isCollapsed || isMobile) && <span className="sidebar-label">{item.label}</span>}
            </a>
          ))}
        </nav>
      </aside>
    </>
  );
}
