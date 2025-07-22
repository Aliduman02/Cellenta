import React, { useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./LoginPage";
import ForgotPassword from "./ForgotPassword";
import Dashboard from "./components/Dashboard";
import Store from "./Store";
import Bills from "./Bills";
import Profile from "./Profile";
import apiService from "./services/api";

// Protected Route Component
function ProtectedRoute({ children }) {
  const userPhone = localStorage.getItem('userPhone');
  const userId = localStorage.getItem('userId');
  
  if (!userPhone || !userId) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
}

// Public Route Component (login sayfaları için)
function PublicRoute({ children }) {
  const userPhone = localStorage.getItem('userPhone');
  const userId = localStorage.getItem('userId');
  
  if (userPhone && userId) {
    return <Navigate to="/dashboard" replace />;
  }
  
  return children;
}

// Ana sayfa component'i - user kontrolü yapar
function HomePage() {
  const userPhone = localStorage.getItem('userPhone');
  const userId = localStorage.getItem('userId');
  
  if (userPhone && userId) {
    return <Navigate to="/dashboard" replace />;
  } else {
    return <Navigate to="/login" replace />;
  }
}

function App() {
  useEffect(() => {
    const checkSessionTimeout = () => {
      const loginTimestamp = localStorage.getItem('loginTimestamp');
      if (loginTimestamp) {
        const now = Date.now();
        const diff = now - parseInt(loginTimestamp, 10);
        if (diff > 3600 * 1000) { // 1 saat
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    };
    checkSessionTimeout();
    const interval = setInterval(checkSessionTimeout, 60 * 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <Router>
      <Routes>
        {/* Ana sayfa - token kontrolü */}
        <Route path="/" element={<HomePage />} />
        
        {/* Public Routes */}
        <Route path="/login" element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } />
        <Route path="/forgot-password" element={
          <PublicRoute>
            <ForgotPassword />
          </PublicRoute>
        } />
        
        {/* Protected Routes */}
        <Route path="/dashboard" element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        } />
        <Route path="/store" element={
          <ProtectedRoute>
            <Store />
          </ProtectedRoute>
        } />
        <Route path="/bills" element={
          <ProtectedRoute>
            <Bills />
          </ProtectedRoute>
        } />
        <Route path="/profile" element={
          <ProtectedRoute>
            <Profile />
          </ProtectedRoute>
        } />
        
        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
