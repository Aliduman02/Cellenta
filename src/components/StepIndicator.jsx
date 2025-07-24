import React from 'react';

export function StepIndicator({ currentStep = 1 }) {
  return (
    <div style={{
      width: '100%',
      marginBottom: '24px',
      display: 'flex',
      justifyContent: 'center'
    }}>
      <div style={{
        display: 'flex',
        gap: '12px',
        alignItems: 'center'
      }}>
        {/* Step 1 Line */}
        <div style={{
          width: '40px',
          height: '4px',
          borderRadius: '2px',
          background: 'linear-gradient(90deg, #0072ff, #00c6ff)',
          boxShadow: '0 2px 8px rgba(0, 114, 255, 0.3)',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          opacity: 1
        }} />

        {/* Step 2 Line */}
        <div style={{
          width: '40px',
          height: '4px',
          borderRadius: '2px',
          background: currentStep >= 2 
            ? 'linear-gradient(90deg, #0072ff, #00c6ff)' 
            : '#e5e7eb',
          boxShadow: currentStep >= 2 
            ? '0 2px 8px rgba(0, 114, 255, 0.3)' 
            : 'none',
          transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
          position: 'relative',
          overflow: 'hidden'
        }}>
          {/* Shimmer Effect for Step 2 */}
          {currentStep >= 2 && (
            <div style={{
              position: 'absolute',
              top: 0,
              left: '-100%',
              width: '100%',
              height: '100%',
              background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.6), transparent)',
              animation: 'shimmer 1.5s infinite',
              WebkitAnimation: 'shimmer 1.5s infinite',
              MozAnimation: 'shimmer 1.5s infinite',
              msAnimation: 'shimmer 1.5s infinite'
            }} />
          )}
        </div>
      </div>

      <style>{`
        @keyframes shimmer {
          0% {
            left: -100%;
          }
          100% {
            left: 100%;
          }
        }
        
        @-webkit-keyframes shimmer {
          0% { -webkit-transform: translateX(-100%); }
          100% { -webkit-transform: translateX(100%); }
        }
        
        @-moz-keyframes shimmer {
          0% { -moz-transform: translateX(-100%); }
          100% { -moz-transform: translateX(100%); }
        }
        
        @-ms-keyframes shimmer {
          0% { -ms-transform: translateX(-100%); }
          100% { -ms-transform: translateX(100%); }
        }
      `}</style>
    </div>
  );
} 