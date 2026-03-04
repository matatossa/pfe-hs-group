import { useState, useEffect } from 'react';
import { rssApi, normApi } from '../api.js';

function StatCard({ label, value, icon, color, sub }) {
    return (
        <div className={`stat-card ${color}`}>
            <div className="stat-header">
                <span className="stat-label">{label}</span>
                <div className="stat-icon">{icon}</div>
            </div>
            <div className="stat-value">{value ?? '—'}</div>
            {sub && <div className="stat-sub">{sub}</div>}
        </div>
    );
}

const SEV_ORDER = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO', 'UNKNOWN'];
const SEV_COLORS = { CRITICAL: '#ff4757', HIGH: '#f0883e', MEDIUM: '#e3c547', LOW: '#3fb950', INFO: '#58a6ff', UNKNOWN: '#8b949e' };

function SeverityBar({ sevMap, total }) {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {SEV_ORDER.map(sev => {
                const count = sevMap[sev] || 0;
                const pct = total ? Math.round((count / total) * 100) : 0;
                return (
                    <div key={sev} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <span style={{ fontSize: 11, fontWeight: 600, color: SEV_COLORS[sev], width: 65, textTransform: 'uppercase', letterSpacing: '.5px' }}>{sev}</span>
                        <div style={{ flex: 1 }} className="progress-bar">
                            <div className="progress-fill" style={{ width: `${pct}%`, background: SEV_COLORS[sev], opacity: .85 }} />
                        </div>
                        <span style={{ fontSize: 12, color: 'var(--text-secondary)', width: 30, textAlign: 'right' }}>{count}</span>
                    </div>
                );
            })}
        </div>
    );
}

function RecentVulnRow({ vuln }) {
    const sev = (vuln.severity || 'UNKNOWN').toUpperCase();
    return (
        <tr>
            <td>
                <div style={{ maxWidth: 260, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {vuln.title || '(no title)'}
                </div>
            </td>
            <td>
                <span className={`severity-badge sev-${sev}`}>
                    {sev}
                </span>
            </td>
            <td>{vuln.cveId ? <span className="cve-tag">{vuln.cveId}</span> : <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>—</span>}</td>
            <td style={{ color: 'var(--text-secondary)', fontSize: 12 }}>{vuln.source || '—'}</td>
            <td style={{ color: 'var(--text-muted)', fontSize: 11, fontFamily: 'JetBrains Mono, monospace' }}>
                {vuln.publishedAt ? new Date(vuln.publishedAt).toLocaleDateString() : '—'}
            </td>
        </tr>
    );
}

export default function DashboardPage({ health }) {
    const [rssStats, setRssStats] = useState(null);
    const [normStats, setNormStats] = useState(null);
    const [recentVulns, setRecentVulns] = useState([]);
    const [sevMap, setSevMap] = useState({});
    const [status, setStatus] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        load();
    }, []);

    async function load() {
        setLoading(true);
        try {
            const [rs, ns, vulns, st] = await Promise.allSettled([
                rssApi.getEntryStats(),
                normApi.getStats(),
                normApi.getAll(),
                rssApi.getStatus(),
            ]);
            if (rs.status === 'fulfilled') setRssStats(rs.value);
            if (ns.status === 'fulfilled') setNormStats(ns.value);
            if (st.status === 'fulfilled') setStatus(st.value);
            if (vulns.status === 'fulfilled' && Array.isArray(vulns.value)) {
                const list = vulns.value;
                setRecentVulns(list.slice(0, 8));
                const map = {};
                list.forEach(v => {
                    const s = (v.severity || 'UNKNOWN').toUpperCase();
                    map[s] = (map[s] || 0) + 1;
                });
                setSevMap(map);
            }
        } finally {
            setLoading(false);
        }
    }

    const serviceUp = (ok) => ok === null ? '🟡 Checking' : ok ? '🟢 Online' : '🔴 Offline';

    return (
        <div>
            {/* ─── KPI Stats ──────────────────────────────────── */}
            <div className="stats-grid">
                <StatCard
                    label="Feed Sources"
                    value={rssStats?.feedSources ?? '—'}
                    icon="📡"
                    color="blue"
                    sub="Active RSS sources"
                />
                <StatCard
                    label="Total Entries"
                    value={rssStats?.totalEntries ?? '—'}
                    icon="📄"
                    color="purple"
                    sub={rssStats ? `${rssStats.processedEntries} processed` : undefined}
                />
                <StatCard
                    label="Vulnerabilities"
                    value={normStats?.totalVulnerabilities ?? '—'}
                    icon="🛡️"
                    color="orange"
                    sub={normStats ? `${normStats.relevantVulnerabilities} relevant` : undefined}
                />
                <StatCard
                    label="Unprocessed"
                    value={rssStats?.unprocessedEntries ?? '—'}
                    icon="⚠️"
                    color={rssStats?.unprocessedEntries > 0 ? 'red' : 'green'}
                    sub="Pending normalization"
                />
            </div>

            {/* ─── Services + Severity ─────────────────────────── */}
            <div className="two-col" style={{ marginBottom: 20 }}>
                {/* Service Health */}
                <div className="table-container" style={{ padding: 18 }}>
                    <div className="section-header" style={{ marginBottom: 16 }}>
                        <span className="section-title">🔌 Service Health</span>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                        {[
                            { name: 'RSS Collector', port: '8081', ok: health.rss },
                            { name: 'Normalization', port: '8082', ok: health.norm },
                        ].map(s => (
                            <div key={s.name} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 14px', background: 'var(--bg-tertiary)', borderRadius: 10, border: '1px solid var(--border-muted)' }}>
                                <div>
                                    <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 2 }}>{s.name}</div>
                                    <div style={{ fontSize: 11, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>localhost:{s.port}</div>
                                </div>
                                <span style={{ fontSize: 12, fontWeight: 600, color: s.ok === null ? 'var(--severity-medium)' : s.ok ? 'var(--accent-green)' : 'var(--severity-critical)' }}>
                                    {serviceUp(s.ok)}
                                </span>
                            </div>
                        ))}

                        {status && (
                            <div style={{ padding: '12px 14px', background: 'var(--bg-tertiary)', borderRadius: 10, border: '1px solid var(--border-muted)', fontSize: 12 }}>
                                <div style={{ fontWeight: 600, marginBottom: 8, color: 'var(--text-secondary)' }}>Last Collection Run</div>
                                <div style={{ display: 'flex', gap: 16 }}>
                                    <div><span style={{ color: 'var(--text-muted)' }}>Items: </span><span style={{ color: 'var(--accent-blue)', fontWeight: 600 }}>{status.itemsCollected ?? '—'}</span></div>
                                    <div><span style={{ color: 'var(--text-muted)' }}>Sources: </span><span style={{ color: 'var(--accent-blue)', fontWeight: 600 }}>{status.feedsProcessed ?? '—'}</span></div>
                                    {status.status && <div><span className={`status-badge ${status.status === 'SUCCESS' ? 'status-active' : 'status-inactive'}`}>{status.status}</span></div>}
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Severity Breakdown */}
                <div className="table-container" style={{ padding: 18 }}>
                    <div className="section-header" style={{ marginBottom: 16 }}>
                        <span className="section-title">📊 Severity Distribution</span>
                    </div>
                    {loading ? (
                        <div className="loading-spinner"><div className="spinner" /></div>
                    ) : (
                        <SeverityBar sevMap={sevMap} total={normStats?.totalVulnerabilities || Object.values(sevMap).reduce((a, b) => a + b, 0)} />
                    )}
                </div>
            </div>

            {/* ─── Recent Vulnerabilities ──────────────────────── */}
            <div>
                <div className="section-header">
                    <span className="section-title">🔴 Recent Vulnerabilities <span className="section-count">{recentVulns.length}</span></span>
                </div>
                <div className="table-container">
                    {loading ? (
                        <div className="loading-spinner"><div className="spinner" /></div>
                    ) : recentVulns.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-icon">🛡️</div>
                            <span>No vulnerabilities found — services may be starting up</span>
                        </div>
                    ) : (
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Title</th>
                                    <th>Severity</th>
                                    <th>CVE ID</th>
                                    <th>Source</th>
                                    <th>Published</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentVulns.map(v => <RecentVulnRow key={v.id} vuln={v} />)}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
}
