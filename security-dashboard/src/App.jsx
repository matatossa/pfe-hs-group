import { useState, useEffect } from 'react';
import DashboardPage from './pages/DashboardPage.jsx';
import VulnerabilitiesPage from './pages/VulnerabilitiesPage.jsx';
import FeedSourcesPage from './pages/FeedSourcesPage.jsx';
import RawEntriesPage from './pages/RawEntriesPage.jsx';
import EurekaPage from './pages/EurekaPage.jsx';
import { checkHealth } from './api.js';

const NAV_ITEMS = [
  { id: 'dashboard', label: 'Overview', icon: '📊', group: 'main' },
  { id: 'registry', label: 'Service Registry', icon: '🗂️', group: 'main' },
  { id: 'vulnerabilities', label: 'Vulnerabilities', icon: '🛡️', group: 'normalization' },
  { id: 'feeds', label: 'Feed Sources', icon: '📡', group: 'collector' },
  { id: 'entries', label: 'Raw Entries', icon: '📄', group: 'collector' },
];

export default function App() {
  const [page, setPage] = useState('dashboard');
  const [health, setHealth] = useState({ rss: null, norm: null });
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    checkHealth().then(setHealth);
    const interval = setInterval(() => checkHealth().then(setHealth), 15000);
    return () => clearInterval(interval);
  }, []);

  function addToast(msg, type = 'info') {
    const id = Date.now();
    setToasts(p => [...p, { id, msg, type }]);
    setTimeout(() => setToasts(p => p.filter(t => t.id !== id)), 4000);
  }

  const groups = [...new Set(NAV_ITEMS.map(n => n.group))];

  const groupLabels = { main: 'Main', collector: 'RSS Collector', normalization: 'Normalization' };

  function renderPage() {
    const props = { toast: addToast };
    switch (page) {
      case 'dashboard': return <DashboardPage      {...props} health={health} />;
      case 'registry': return <EurekaPage          {...props} />;
      case 'vulnerabilities': return <VulnerabilitiesPage {...props} />;
      case 'feeds': return <FeedSourcesPage    {...props} />;
      case 'entries': return <RawEntriesPage     {...props} />;
      default: return <DashboardPage      {...props} health={health} />;
    }
  }

  const pageTitles = {
    dashboard: 'Security Overview',
    registry: 'Service Registry — Eureka',
    vulnerabilities: 'Vulnerabilities',
    feeds: 'RSS Feed Sources',
    entries: 'Raw Feed Entries',
  };

  const serviceStatus = (online) =>
    online === null ? 'checking' : online ? 'online' : 'offline';

  return (
    <div className="app-layout">
      {/* ─── Sidebar ─────────────────────────────────── */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div className="logo-icon">🔒</div>
          <span className="logo-text">SecureFeed</span>
          <span className="logo-version">v1.0</span>
        </div>

        <nav className="sidebar-nav">
          {groups.map(g => (
            <div key={g}>
              <div className="nav-group-label">{groupLabels[g]}</div>
              {NAV_ITEMS.filter(n => n.group === g).map(n => (
                <div
                  key={n.id}
                  className={`nav-item${page === n.id ? ' active' : ''}`}
                  onClick={() => setPage(n.id)}
                >
                  <span className="nav-icon">{n.icon}</span>
                  {n.label}
                </div>
              ))}
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="nav-group-label" style={{ marginBottom: 8 }}>Services</div>
          <div className="service-indicator">
            <div className={`service-dot ${serviceStatus(health.rss)}`} />
            RSS Collector
            <span style={{ marginLeft: 'auto', fontSize: 10, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>
              :8081
            </span>
          </div>
          <div className="service-indicator">
            <div className={`service-dot ${serviceStatus(health.norm)}`} />
            Normalization
            <span style={{ marginLeft: 'auto', fontSize: 10, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>
              :8082
            </span>
          </div>
          <div className="service-indicator">
            <div className="service-dot" style={{ background: 'var(--accent-purple)', boxShadow: '0 0 6px var(--accent-purple)' }} />
            Eureka
            <span style={{ marginLeft: 'auto', fontSize: 10, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>
              :8761
            </span>
          </div>
        </div>
      </aside>

      {/* ─── Main ────────────────────────────────────── */}
      <div className="main-content">
        <header className="topbar">
          <h1 className="topbar-title">{pageTitles[page]}</h1>
          <div style={{ marginLeft: 'auto', display: 'flex', gap: 10 }}>
            <span style={{ fontSize: 11, color: 'var(--text-muted)', alignSelf: 'center', fontFamily: 'JetBrains Mono, monospace' }}>
              {new Date().toLocaleString()}
            </span>
          </div>
        </header>

        <main className="page-content animate-in">
          {renderPage()}
        </main>
      </div>

      {/* ─── Toast Container ─────────────────────────── */}
      <div className="toast-container">
        {toasts.map(t => (
          <div key={t.id} className={`toast ${t.type}`}>
            <span>{t.type === 'success' ? '✅' : t.type === 'error' ? '❌' : t.type === 'warning' ? '⚠️' : 'ℹ️'}</span>
            {t.msg}
          </div>
        ))}
      </div>
    </div>
  );
}
