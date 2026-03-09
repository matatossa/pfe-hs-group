import { useState, useEffect, useCallback, useRef } from 'react';
import { createPortal } from 'react-dom';
import { normApi } from '../api.js';

const SEVERITY_LEVELS = ['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
const SEV_CHIP_CLASS = { CRITICAL: 'crit', HIGH: 'high', MEDIUM: 'med', LOW: 'low' };

function VulnDetailModal({ vuln, onClose }) {
    const sev = (vuln.severity || 'UNKNOWN').toUpperCase();

    // Lock body scroll when open
    useEffect(() => {
        document.body.style.overflow = 'hidden';
        return () => { document.body.style.overflow = ''; };
    }, []);

    const modal = (
        <div
            className="modal-overlay"
            style={{ zIndex: 9999 }}
            onClick={e => e.target === e.currentTarget && onClose()}
        >
            <div className="modal">
                <div className="modal-header">
                    <span className="modal-title">Vulnerability Detail</span>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>
                <div className="modal-body">
                    <div style={{ marginBottom: 16 }}>
                        <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 10, lineHeight: 1.5 }}>
                            {vuln.title}
                        </div>
                        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                            <span className={`severity-badge sev-${sev}`}>{sev}</span>
                            {vuln.cveId && <span className="cve-tag">{vuln.cveId}</span>}
                            {vuln.isRelevant && <span className="status-badge status-relevant">✓ Relevant</span>}
                        </div>
                    </div>

                    {[
                        ['Source', vuln.source],
                        ['Product', vuln.product !== 'Unknown' ? vuln.product : null],
                        ['CVSS Score', vuln.cvssScore != null ? vuln.cvssScore : null],
                        ['Risk Score', vuln.riskScore != null ? vuln.riskScore : null],
                        ['Relevance', vuln.relevanceScore != null ? (vuln.relevanceScore * 100).toFixed(1) + '%' : null],
                        ['Published', vuln.publishedAt ? new Date(vuln.publishedAt).toLocaleString() : null],
                        ['Fetched', vuln.fetchedAt ? new Date(vuln.fetchedAt).toLocaleString() : null],
                    ].map(([label, value]) => value != null && (
                        <div key={label} className="detail-row">
                            <span className="detail-label">{label}</span>
                            <span className="detail-value"
                                style={label === 'CVSS Score' && vuln.cvssScore > 7
                                    ? { color: 'var(--severity-high)', fontWeight: 600 } : {}}>
                                {value}
                            </span>
                        </div>
                    ))}

                    {vuln.description && (
                        <div className="detail-row" style={{ flexDirection: 'column', gap: 6 }}>
                            <span className="detail-label">Description</span>
                            <span className="detail-value" style={{ fontSize: 12.5, color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                                {vuln.description}
                            </span>
                        </div>
                    )}
                    {vuln.url && (
                        <div className="detail-row">
                            <span className="detail-label">URL</span>
                            <a href={vuln.url} target="_blank" rel="noreferrer"
                                className="detail-value"
                                style={{ color: 'var(--accent-blue)', textDecoration: 'none', fontSize: 12, fontFamily: 'JetBrains Mono, monospace', wordBreak: 'break-all' }}>
                                {vuln.url}
                            </a>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );

    return createPortal(modal, document.body);
}

export default function VulnerabilitiesPage({ toast }) {
    const [allVulns, setAllVulns] = useState([]);
    const [loading, setLoading] = useState(true);
    const [sevFilter, setSevFilter] = useState('ALL');
    const [relevantOnly, setRelOnly] = useState(false);
    // ── Product dropdown open/close ───────────────────────────────
    const [productDropOpen, setProductDropOpen] = useState(false);
    const productDropRef = useRef(null);
    useEffect(() => {
        function handler(e) { if (productDropRef.current && !productDropRef.current.contains(e.target)) setProductDropOpen(false); }
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);
    const [search, setSearch] = useState('');
    const [selected, setSelected] = useState(null);
    const [stats, setStats] = useState(null);
    const [selectedProducts, setSelectedProducts] = useState(new Set()); // empty = all
    const [dateFrom, setDateFrom] = useState('');
    const [dateTo, setDateTo] = useState('');

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const [data, st] = await Promise.all([normApi.getAll(), normApi.getStats()]);
            setAllVulns(data || []);
            setStats(st);
        } catch (e) {
            toast('Failed to load vulnerabilities: ' + e.message, 'error');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { load(); }, [load]);

    // ── Unique products + per-product counts ───────────────────────────
    const productOptions = Array.from(
        new Set(allVulns.map(v => v.product).filter(p => p && p !== 'Unknown'))
    ).sort();

    const productCounts = allVulns.reduce((acc, v) => {
        if (v.product && v.product !== 'Unknown') acc[v.product] = (acc[v.product] || 0) + 1;
        return acc;
    }, {});

    const toggleProduct = prod => setSelectedProducts(prev => {
        const next = new Set(prev);
        next.has(prod) ? next.delete(prod) : next.add(prod);
        return next;
    });

    // ── CLIENT-SIDE filtering ──────────────────────────────────────────
    const displayed = allVulns.filter(v => {
        const sev = (v.severity || 'LOW').toUpperCase();
        if (sevFilter !== 'ALL' && sev !== sevFilter) return false;
        if (relevantOnly && !v.isRelevant) return false;
        if (selectedProducts.size > 0 && !selectedProducts.has(v.product)) return false;
        if (dateFrom) {
            const pub = v.publishedAt ? new Date(v.publishedAt) : null;
            if (!pub || pub < new Date(dateFrom)) return false;
        }
        if (dateTo) {
            const pub = v.publishedAt ? new Date(v.publishedAt) : null;
            // add 1 day so "to" is inclusive
            const to = new Date(dateTo);
            to.setDate(to.getDate() + 1);
            if (!pub || pub > to) return false;
        }
        if (search.trim().length >= 2) {
            const q = search.trim().toLowerCase();
            return v.title?.toLowerCase().includes(q)
                || v.cveId?.toLowerCase().includes(q)
                || v.product?.toLowerCase().includes(q)
                || v.source?.toLowerCase().includes(q);
        }
        return true;
    });

    // ── Per-severity counts for filter chips ──────────────────────────
    const counts = allVulns.reduce((acc, v) => {
        const s = (v.severity || 'LOW').toUpperCase();
        acc[s] = (acc[s] || 0) + 1;
        return acc;
    }, {});

    const getColor = (sev) => {
        const s = (sev || '').toUpperCase();
        if (s === 'CRITICAL') return 'var(--severity-critical)';
        if (s === 'HIGH') return 'var(--severity-high)';
        if (s === 'MEDIUM') return 'var(--severity-medium)';
        if (s === 'LOW') return 'var(--severity-low)';
        return 'var(--text-muted)';
    };

    return (
        <div>
            {/* ─── Stats row ─────────────────────────────────────── */}
            {stats && (
                <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 20 }}>
                    <div className="stat-card blue">
                        <div className="stat-header"><span className="stat-label">Total</span><div className="stat-icon">🛡️</div></div>
                        <div className="stat-value">{allVulns.length}</div>
                    </div>
                    <div className="stat-card green">
                        <div className="stat-header"><span className="stat-label">Relevant</span><div className="stat-icon">✅</div></div>
                        <div className="stat-value">{allVulns.filter(v => v.isRelevant).length}</div>
                    </div>
                    <div className="stat-card orange">
                        <div className="stat-header"><span className="stat-label">Filtered Out</span><div className="stat-icon">❌</div></div>
                        <div className="stat-value">{allVulns.filter(v => !v.isRelevant).length}</div>
                    </div>
                </div>
            )}

            {/* ─── Filters ──────────────────────────────────────── */}
            <div style={{ display: 'flex', gap: 10, marginBottom: 16, flexWrap: 'wrap', alignItems: 'center' }}>
                <div className="search-bar" style={{ width: 240, marginRight: 4 }}>
                    <span className="search-icon">🔍</span>
                    <input
                        className="search-input"
                        placeholder="Search title / CVE / product…"
                        value={search}
                        onChange={e => { setSearch(e.target.value); setSevFilter('ALL'); setRelOnly(false); }}
                    />
                </div>

                {/* ─── Severity chips ─── */}
                <div className="filter-bar" style={{ margin: 0 }}>
                    {SEVERITY_LEVELS.map(sev => (
                        <button
                            key={sev}
                            className={`filter-chip ${SEV_CHIP_CLASS[sev] || ''} ${sevFilter === sev && !relevantOnly ? 'active' : ''}`}
                            onClick={() => { setSevFilter(sev); setRelOnly(false); setSearch(''); setSelectedProducts(new Set()); }}
                        >
                            {sev}
                            {sev !== 'ALL' && counts[sev]
                                ? <span style={{ fontSize: 10, opacity: 0.75, marginLeft: 2 }}>({counts[sev]})</span>
                                : null}
                            {sev === 'ALL'
                                ? <span style={{ fontSize: 10, opacity: 0.75, marginLeft: 2 }}>({allVulns.length})</span>
                                : null}
                        </button>
                    ))}
                    <button
                        className={`filter-chip ${relevantOnly ? 'active' : ''}`}
                        onClick={() => { setRelOnly(p => !p); setSevFilter('ALL'); setSearch(''); setSelectedProducts(new Set()); }}
                    >
                        🎯 Relevant only ({allVulns.filter(v => v.isRelevant).length})
                    </button>
                </div>

                {/* ─── Date range ─── */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>From</span>
                    <input
                        type="date"
                        value={dateFrom}
                        onChange={e => setDateFrom(e.target.value)}
                        style={{
                            background: 'var(--bg-card)', color: 'var(--text-secondary)',
                            border: `1px solid ${dateFrom ? 'rgba(88,166,255,0.4)' : 'var(--border-subtle)'}`,
                            borderRadius: 8, padding: '5px 8px', fontFamily: 'JetBrains Mono, monospace',
                            fontSize: 11, cursor: 'pointer', outline: 'none',
                            colorScheme: 'dark',
                        }}
                    />
                    <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>To</span>
                    <input
                        type="date"
                        value={dateTo}
                        min={dateFrom || undefined}
                        onChange={e => setDateTo(e.target.value)}
                        style={{
                            background: 'var(--bg-card)', color: 'var(--text-secondary)',
                            border: `1px solid ${dateTo ? 'rgba(88,166,255,0.4)' : 'var(--border-subtle)'}`,
                            borderRadius: 8, padding: '5px 8px', fontFamily: 'JetBrains Mono, monospace',
                            fontSize: 11, cursor: 'pointer', outline: 'none',
                            colorScheme: 'dark',
                        }}
                    />
                    {(dateFrom || dateTo) && (
                        <button onClick={() => { setDateFrom(''); setDateTo(''); }}
                            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: 13 }}
                            title="Clear date filter">✕</button>
                    )}
                </div>

                <button className="topbar-btn btn-ghost" style={{ marginLeft: 'auto' }}
                    onClick={() => { setSearch(''); setSevFilter('ALL'); setRelOnly(false); setSelectedProducts(new Set()); setDateFrom(''); setDateTo(''); }}>
                    ↺ Reset
                </button>
            </div>

            {/* ─── Product multi-select dropdown ─── */}
            {productOptions.length > 0 && (
                <div ref={productDropRef} style={{ position: 'relative' }}>
                    <button
                        onClick={() => setProductDropOpen(o => !o)}
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: 6,
                            background: selectedProducts.size > 0 ? 'rgba(88,166,255,0.12)' : 'var(--bg-card)',
                            border: `1px solid ${selectedProducts.size > 0 ? 'rgba(88,166,255,0.4)' : 'var(--border-subtle)'}`,
                            color: selectedProducts.size > 0 ? 'var(--accent-blue)' : 'var(--text-secondary)',
                            borderRadius: 20, padding: '6px 14px', fontFamily: 'inherit',
                            fontSize: 12, fontWeight: 500, cursor: 'pointer', userSelect: 'none',
                        }}
                    >
                        🏷️ {selectedProducts.size === 0
                            ? 'All Products'
                            : `${selectedProducts.size} Product${selectedProducts.size > 1 ? 's' : ''} selected`
                        } {productDropOpen ? '▲' : '▼'}
                    </button>

                    {productDropOpen && (
                        <div style={{
                            position: 'absolute', top: 'calc(100% + 6px)', left: 0, zIndex: 500,
                            background: 'var(--bg-secondary)', border: '1px solid var(--border-subtle)',
                            borderRadius: 10, minWidth: 220, maxHeight: 320, overflowY: 'auto',
                            boxShadow: '0 8px 24px rgba(0,0,0,0.4)', padding: '8px 0',
                        }}>
                            {selectedProducts.size > 0 && (
                                <button
                                    onClick={() => setSelectedProducts(new Set())}
                                    style={{
                                        display: 'block', width: '100%', textAlign: 'left',
                                        background: 'none', border: 'none', padding: '6px 14px 10px',
                                        fontSize: 11, color: 'var(--accent-blue)', cursor: 'pointer',
                                        borderBottom: '1px solid var(--border-subtle)', marginBottom: 4,
                                    }}
                                >✕ Clear selection</button>
                            )}
                            {productOptions.map(prod => {
                                const active = selectedProducts.has(prod);
                                return (
                                    <label key={prod} style={{
                                        display: 'flex', alignItems: 'center', gap: 10,
                                        padding: '7px 14px', cursor: 'pointer',
                                        background: active ? 'rgba(88,166,255,0.08)' : 'transparent',
                                        transition: 'background 0.12s',
                                    }}>
                                        <input
                                            type="checkbox"
                                            checked={active}
                                            onChange={() => toggleProduct(prod)}
                                            style={{ accentColor: 'var(--accent-blue)', width: 14, height: 14 }}
                                        />
                                        <span style={{ fontSize: 13, color: active ? 'var(--text-primary)' : 'var(--text-secondary)', flex: 1 }}>
                                            {prod}
                                        </span>
                                        <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                                            ({productCounts[prod] || 0})
                                        </span>
                                    </label>
                                );
                            })}
                        </div>
                    )}
                </div>
            )}

            {/* ─── Table ────────────────────────────────────────── */}
            <div className="section-header">
                <span className="section-title">
                    Vulnerabilities <span className="section-count">{displayed.length}</span>
                </span>
            </div>
            <div className="table-container">
                {loading ? (
                    <div className="loading-spinner"><div className="spinner" /><span>Loading vulnerabilities…</span></div>
                ) : displayed.length === 0 ? (
                    <div className="empty-state"><div className="empty-icon">🔍</div>No vulnerabilities match your filter</div>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Severity</th>
                                <th>CVE ID</th>
                                <th>Title</th>
                                <th>Product</th>
                                <th>CVSS</th>
                                <th>Source</th>
                                <th>Relevant</th>
                                <th>Published</th>
                            </tr>
                        </thead>
                        <tbody>
                            {displayed.map(v => {
                                const sev = (v.severity || 'UNKNOWN').toUpperCase();
                                return (
                                    <tr key={v.id}
                                        style={{ cursor: 'pointer', userSelect: 'none' }}
                                        onClick={() => setSelected(v)}>
                                        <td>
                                            <span className={`severity-badge sev-${sev}`}>{sev}</span>
                                        </td>
                                        <td>
                                            {v.cveId
                                                ? <span className="cve-tag">{v.cveId}</span>
                                                : <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>—</span>}
                                        </td>
                                        <td style={{ maxWidth: 380 }}>
                                            <div style={{ fontSize: 13, lineHeight: 1.45, whiteSpace: 'normal', wordBreak: 'break-word' }}>
                                                {v.title || '—'}
                                            </div>
                                        </td>
                                        <td style={{ color: 'var(--text-secondary)', fontSize: 12, maxWidth: 130 }}>
                                            {v.product && v.product !== 'Unknown'
                                                ? v.product
                                                : <span style={{ color: 'var(--text-muted)' }}>—</span>}
                                        </td>
                                        <td>
                                            {v.cvssScore != null
                                                ? <span style={{ fontWeight: 700, color: getColor(v.severity), fontFamily: 'JetBrains Mono, monospace', fontSize: 13 }}>{v.cvssScore}</span>
                                                : <span style={{ color: 'var(--text-muted)' }}>—</span>}
                                        </td>
                                        <td style={{ color: 'var(--text-secondary)', fontSize: 12 }}>{v.source || '—'}</td>
                                        <td>
                                            {v.isRelevant
                                                ? <span style={{ color: 'var(--accent-green)', fontSize: 13 }}>✓</span>
                                                : <span style={{ color: 'var(--text-muted)', fontSize: 13 }}>—</span>}
                                        </td>
                                        <td style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'JetBrains Mono, monospace', whiteSpace: 'nowrap' }}>
                                            {v.publishedAt ? new Date(v.publishedAt).toLocaleDateString() : '—'}
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                )}
            </div>

            {selected && <VulnDetailModal vuln={selected} onClose={() => setSelected(null)} />}
        </div >
    );
}
