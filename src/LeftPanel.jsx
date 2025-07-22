export function LeftPanel() {
  return (
    <div className="left-content">
      <div
        className="logo-section"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          marginBottom: '32px',
        }}
      >
        <img
          src="/images/icon-white.png"
          alt="Logo"
          className="logo-icon"
          style={{ height: '88px', objectFit: 'contain' }}
        />
        <img
          src="/images/title-white.png"
          alt="CELLENTA Title"
          className="logo-title"
          style={{ height: '48px', objectFit: 'contain' }}
        />
      </div>
      <h2 className="left-slogan">Connecting You to Tomorrow</h2>
      <p className="description">
        Experience seamless communication, reliable coverage, and innovative
        solutions for a truly connected world.
      </p>
    </div>
  );
}