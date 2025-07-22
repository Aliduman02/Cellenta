import { Home, Package, FileText, User } from "lucide-react";
import { Dock, DockIcon, DockItem } from "./Dock";
import React from "react";

const data = [
  { title: "Home", icon: <Home />, href: "/dashboard" },
  { title: "Store", icon: <Package />, href: "/store" },
  { title: "Bills", icon: <FileText />, href: "/bills" },
  { title: "Profile", icon: <User />, href: "/profile" },
];

function isActive(href) {
  return window.location.pathname === href;
}

export default function AppleStyleDock() {
  return (
    <div style={{ position: "fixed", left: 48, top: "50%", transform: "translateY(-50%)", zIndex: 100 }}>
      <Dock className="flex-col items-start gap-4" style={{ minWidth: 140 }}>
        {data.map((item, idx) => {
          const active = isActive(item.href);
          return (
            <a
              key={idx}
              href={item.href}
              style={{
                textDecoration: "none",
                width: "100%",
                display: "flex",
                alignItems: "center",
                gap: 8,
              }}
            >
              <DockItem active={active}
                className="flex flex-row items-center gap-2 px-3 py-2 rounded-xl transition-all"
                style={{
                  width: 140,
                  minHeight: 44,
                  padding: "8px 14px",
                  borderRadius: 14,
                  background: active ? "rgba(245,243,255,0.85)" : "rgba(255,255,255,0.7)",
                  boxShadow: active ? "0 4px 16px rgba(124,60,237,0.10)" : "0 2px 8px rgba(0,0,0,0.04)",
                  border: active ? "2px solid #a78bfa" : "2px solid transparent",
                  fontWeight: active ? 700 : 500,
                  cursor: "pointer",
                }}
              >
                <DockIcon active={active}>{item.icon}</DockIcon>
                <span
                  style={{
                    fontSize: 15,
                    color: active ? "#7c3aed" : "#222",
                    fontWeight: active ? 700 : 500,
                    marginLeft: 8,
                    letterSpacing: 0.2,
                  }}
                >
                  {item.title}
                </span>
              </DockItem>
            </a>
          );
        })}
      </Dock>
    </div>
  );
}
