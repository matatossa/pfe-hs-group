import { useState, useEffect } from 'react';

const EUREKA_BASE = 'http://localhost:8761';

// Eureka returns XML or JSON depending on Accept header
export async function fetchEurekaApps() {
    const res = await fetch(`${EUREKA_BASE}/eureka/apps`, {
        headers: { Accept: 'application/json' },
    });
    if (!res.ok) throw new Error(`Eureka HTTP ${res.status}`);
    const data = await res.json();
    // Navigate to the list of apps
    const apps = data?.applications?.application;
    if (!apps) return [];
    return Array.isArray(apps) ? apps : [apps];
}

const STATUS_COLORS = {
    UP: { bg: 'rgba(63,185,80,0.12)', text: 'var(--accent-green)', border: 'rgba(63,185,80,0.3)' },
    DOWN: { bg: 'rgba(255,71,87,0.12)', text: 'var(--severity-critical)', border: 'rgba(255,71,87,0.3)' },
    STARTING: { bg: 'rgba(227,197,71,0.12)', text: 'var(--severity-medium)', border: 'rgba(227,197,71,0.3)' },
    OUT_OF_SERVICE: { bg: 'rgba(139,148,158,0.1)', text: 'var(--text-muted)', border: 'rgba(139,148,158,0.3)' },
    UNKNOWN: { bg: 'rgba(139,148,158,0.1)', text: 'var(--text-muted)', border: 'rgba(139,148,158,0.3)' },
};

function StatusPill({ status }) {
    const c = STATUS_COLORS[status] || STATUS_COLORS.UNKNOWN;
    return (
        <span style={{
            display: 'inline-flex', alignItems: 'center', gap: 6,
            padding: '3px 10px', borderRadius: 20, fontSize: 11.5,
            fontWeight: 700, letterSpacing: 0.5,
            background: c.bg, color: c.text, border: `1px solid ${c.border}`,
        }}>
            <span style={{
                width: 7, height: 7, borderRadius: '50%', background: c.text, display: 'inline-block',
                boxShadow: status === 'UP' ? `0 0 6px ${c.text}` : 'none',
                animation: status === 'UP' ? 'pulse-dot 2s infinite' : 'none'
            }} />
            {status}
        </span>
    );
}

function InstanceCard({ app, instance }) {
    const ip = instance.ipAddr || instance.hostName || '—';
    const port = instance.port?.['$'] || instance.port || '—';
    const status = instance.status || 'UNKNOWN';
    const lastRenewed = instance.lastUpdatedTimestamp
        ? new Date(Number(instance.lastUpdatedTimestamp)).toLocaleString()
        : '—';

    return (
        <div style={{
            background: 'var(--bg-tertiary)',
            border: '1px solid var(--border-muted)',
            borderRadius: 10,
            padding: '14px 16px',
            display: 'flex',
            flexDirection: 'column',
            gap: 10,
        }}>
            {/* Instance header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 10 }}>
                <div>
                    <div style={{ fontWeight: 600, fontSize: 13, color: 'var(--text-primary)', marginBottom: 2 }}>
                        {instance.instanceId || `${ip}:${port}`}
                    </div>
                    <div style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11, color: 'var(--text-muted)' }}>
                        {ip}:{port}
                    </div>
                </div>
                <StatusPill status={status} />
            </div>

            {/* Details row */}
            <div style={{ display: 'flex', gap: 16, fontSize: 12, color: 'var(--text-secondary)', flexWrap: 'wrap' }}>
                {instance.homePageUrl && (
                    <a
                        href={instance.homePageUrl}
                        target="_blank"
                        rel="noreferrer"
                        style={{ color: 'var(--accent-cyan)', textDecoration: 'none', fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}
                    >
                        🔗 Home
                    </a>
                )}
                {instance.healthCheckUrl && (
                    <a
                        href={instance.healthCheckUrl}
                        target="_blank"
                        rel="noreferrer"
                        style={{ color: 'var(--accent-green)', textDecoration: 'none', fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}
                    >
                        ❤️ Health
                    </a>
                )}
                <span style={{ marginLeft: 'auto', fontSize: 11, color: 'var(--text-muted)' }}>
                    Last renewed: {lastRenewed}
                </span>
            </div>
        </div>
    );
}

function ServiceCard({ app }) {
    const instances = Array.isArray(app.instance) ? app.instance : [app.instance];
    const upCount = instances.filter(i => i.status === 'UP').length;

    return (
        <div className="feed-card animate-in" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {/* App header */}
            <div className="feed-card-header">
                <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
                        <span style={{ fontSize: 20 }}>⚙️</span>
                        <div className="feed-card-name" style={{ fontSize: 15, textTransform: 'uppercase', letterSpacing: 0.8 }}>
                            {app.name}
                        </div>
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                        {instances.length} instance{instances.length !== 1 ? 's' : ''} &nbsp;·&nbsp;
                        <span style={{ color: upCount > 0 ? 'var(--accent-green)' : 'var(--severity-critical)', fontWeight: 600 }}>
                            {upCount} UP
                        </span>
                    </div>
                </div>
                <div style={{
                    width: 40, height: 40, borderRadius: '50%',
                    background: upCount > 0 ? 'rgba(63,185,80,0.15)' : 'rgba(255,71,87,0.15)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 18,
                }}>
                    {upCount > 0 ? '✅' : '❌'}
                </div>
            </div>

            {/* Instance cards */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {instances.map((inst, i) => (
                    <InstanceCard key={inst.instanceId || i} app={app} instance={inst} />
                ))}
            </div>
        </div>
    );
}

export default function EurekaPage({ toast }) {
    const [apps, setApps] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [lastRefresh, setLastRefresh] = useState(null);

    async function load() {
        setLoading(true);
        setError(null);
        try {
            const data = await fetchEurekaApps();
            setApps(data);
            setLastRefresh(new Date());
        } catch (e) {
            setError(e.message);
            toast('Cannot reach Eureka server at :8761 — is it running?', 'warning');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        load();
        const interval = setInterval(load, 15000); // auto-refresh every 15s
        return () => clearInterval(interval);
    }, []);

    const totalInstances = apps.reduce((sum, a) => {
        const instances = Array.isArray(a.instance) ? a.instance : [a.instance];
        return sum + instances.length;
    }, 0);
    const upInstances = apps.reduce((sum, a) => {
        const instances = Array.isArray(a.instance) ? a.instance : [a.instance];
        return sum + instances.filter(i => i.status === 'UP').length;
    }, 0);

    return (
        <div>
            {/* ─── Top Stats ────────────────────────────────────── */}
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3,1fr)', marginBottom: 20 }}>
                <div className="stat-card blue">
                    <div className="stat-header"><span className="stat-label">Registered Services</span><div className="stat-icon">📋</div></div>
                    <div className="stat-value">{apps.length}</div>
                </div>
                <div className="stat-card green">
                    <div className="stat-header"><span className="stat-label">Instances UP</span><div className="stat-icon">✅</div></div>
                    <div className="stat-value">{upInstances}</div>
                    <div className="stat-sub">of {totalInstances} total</div>
                </div>
                <div className="stat-card purple">
                    <div className="stat-header"><span className="stat-label">Eureka Server</span><div className="stat-icon">🗂️</div></div>
                    <div className="stat-value" style={{ fontSize: 18 }}>
                        {error ? <span style={{ color: 'var(--severity-critical)', fontSize: 14 }}>Offline</span> : <span style={{ color: 'var(--accent-green)' }}>Online</span>}
                    </div>
                    <div className="stat-sub" style={{ fontFamily: 'JetBrains Mono, monospace' }}>localhost:8761</div>
                </div>
            </div>

            {/* ─── Header + Refresh ─────────────────────────────── */}
            <div className="section-header" style={{ marginBottom: 16 }}>
                <span className="section-title">
                    Registered Instances
                    <span className="section-count">{apps.length}</span>
                </span>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    {lastRefresh && (
                        <span style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'JetBrains Mono, monospace' }}>
                            refreshed {lastRefresh.toLocaleTimeString()}
                        </span>
                    )}
                    <button className="topbar-btn btn-ghost" onClick={load} disabled={loading}>
                        {loading ? '⏳' : '↺'} Refresh
                    </button>
                    <a
                        href={`${EUREKA_BASE}/`}
                        target="_blank"
                        rel="noreferrer"
                        className="topbar-btn btn-primary"
                        style={{ textDecoration: 'none' }}
                    >
                        🔗 Open Eureka UI
                    </a>
                </div>
            </div>

            {/* ─── Content ──────────────────────────────────────── */}
            {loading && apps.length === 0 ? (
                <div className="table-container">
                    <div className="loading-spinner">
                        <div className="spinner" />
                        <span>Connecting to Eureka server…</span>
                    </div>
                </div>
            ) : error && apps.length === 0 ? (
                <div className="table-container">
                    <div className="empty-state" style={{ padding: 48 }}>
                        <div className="empty-icon">🔌</div>
                        <span style={{ fontWeight: 600, color: 'var(--severity-critical)' }}>Eureka server unreachable</span>
                        <span style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>
                            Make sure eureka-server is running on port 8761
                        </span>
                        <button className="topbar-btn btn-primary" style={{ marginTop: 16 }} onClick={load}>
                            ↺ Retry
                        </button>
                    </div>
                </div>
            ) : apps.length === 0 ? (
                <div className="table-container">
                    <div className="empty-state">
                        <div className="empty-icon">📭</div>
                        <span>No services registered yet — start your microservices</span>
                    </div>
                </div>
            ) : (
                <div className="cards-grid">
                    {apps.map(app => (
                        <ServiceCard key={app.name} app={app} />
                    ))}
                </div>
            )}
        </div>
    );
}
