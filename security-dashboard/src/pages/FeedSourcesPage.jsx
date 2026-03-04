import { useState, useEffect } from 'react';
import { rssApi } from '../api.js';

function AddFeedModal({ onClose, onSave }) {
    const [form, setForm] = useState({ name: '', url: '', feedType: 'RSS' });
    const [saving, setSaving] = useState(false);

    async function handleSave() {
        if (!form.name.trim() || !form.url.trim()) return;
        setSaving(true);
        try { await onSave(form); } finally { setSaving(false); }
    }

    return (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
            <div className="modal">
                <div className="modal-header">
                    <span className="modal-title">Add Feed Source</span>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>
                <div className="modal-body">
                    <div className="form-group">
                        <label className="form-label">Source Name *</label>
                        <input
                            className="form-input"
                            placeholder="e.g. CERT-FR"
                            value={form.name}
                            onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Feed URL *</label>
                        <input
                            className="form-input"
                            placeholder="https://example.com/rss"
                            value={form.url}
                            onChange={e => setForm(f => ({ ...f, url: e.target.value }))}
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Feed Type</label>
                        <select
                            className="form-input"
                            value={form.feedType}
                            onChange={e => setForm(f => ({ ...f, feedType: e.target.value }))}
                            style={{ cursor: 'pointer' }}
                        >
                            <option value="RSS">RSS</option>
                            <option value="ATOM">ATOM</option>
                        </select>
                    </div>
                </div>
                <div className="form-footer">
                    <button className="topbar-btn btn-ghost" onClick={onClose}>Cancel</button>
                    <button
                        className="topbar-btn btn-primary"
                        onClick={handleSave}
                        disabled={saving || !form.name.trim() || !form.url.trim()}
                    >
                        {saving ? '⏳ Saving…' : '+ Add Source'}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default function FeedSourcesPage({ toast }) {
    const [feeds, setFeeds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showAdd, setShowAdd] = useState(false);
    const [triggering, setTriggering] = useState(false);
    const [status, setStatus] = useState(null);

    useEffect(() => { load(); }, []);

    async function load() {
        setLoading(true);
        try {
            const [f, s] = await Promise.all([rssApi.getFeeds(), rssApi.getStatus()]);
            setFeeds(f || []);
            setStatus(s);
        } catch (e) {
            toast('Failed to load feeds: ' + e.message, 'error');
        } finally {
            setLoading(false);
        }
    }

    async function handleToggle(id) {
        try {
            const updated = await rssApi.toggleFeed(id);
            setFeeds(f => f.map(x => x.id === id ? updated : x));
            toast('Feed status updated', 'success');
        } catch (e) {
            toast('Toggle error: ' + e.message, 'error');
        }
    }

    async function handleAdd(feedData) {
        try {
            const saved = await rssApi.addFeed(feedData);
            setFeeds(f => [...f, saved]);
            setShowAdd(false);
            toast('Feed source added successfully!', 'success');
        } catch (e) {
            toast('Failed to add feed: ' + e.message, 'error');
        }
    }

    async function handleTrigger() {
        setTriggering(true);
        try {
            const result = await rssApi.trigger();
            setStatus(result);
            toast(`Collection triggered! ${result?.itemsCollected ?? 0} items collected.`, 'success');
        } catch (e) {
            toast('Trigger failed: ' + e.message, 'error');
        } finally {
            setTriggering(false);
        }
    }

    const activeCount = feeds.filter(f => f.active).length;
    const inactiveCount = feeds.length - activeCount;

    return (
        <div>
            {/* ─── Header actions ─────────────────────────────── */}
            <div style={{ display: 'flex', gap: 10, marginBottom: 20, alignItems: 'center' }}>
                <div style={{ flex: 1, display: 'flex', gap: 14, fontSize: 13, color: 'var(--text-secondary)' }}>
                    <span>📡 <strong style={{ color: 'var(--text-primary)' }}>{feeds.length}</strong> sources</span>
                    <span>🟢 <strong style={{ color: 'var(--accent-green)' }}>{activeCount}</strong> active</span>
                    <span>⚫ <strong style={{ color: 'var(--text-muted)' }}>{inactiveCount}</strong> inactive</span>
                </div>
                <button
                    className="topbar-btn btn-ghost"
                    onClick={handleTrigger}
                    disabled={triggering}
                >
                    {triggering ? '⏳ Collecting…' : '▶ Trigger Collection'}
                </button>
                <button
                    className="topbar-btn btn-primary"
                    onClick={() => setShowAdd(true)}
                >
                    + Add Source
                </button>
            </div>

            {/* ─── Collection Status ────────────────────────────── */}
            {status && (
                <div style={{
                    background: 'var(--bg-card)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 12,
                    padding: '14px 18px',
                    marginBottom: 20,
                    display: 'flex',
                    gap: 24,
                    fontSize: 13,
                    alignItems: 'center',
                }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-secondary)' }}>Last Run</span>
                    {status.startTime && (
                        <span style={{ color: 'var(--text-muted)', fontSize: 12, fontFamily: 'JetBrains Mono, monospace' }}>
                            {new Date(status.startTime).toLocaleString()}
                        </span>
                    )}
                    <span>
                        <strong style={{ color: 'var(--accent-blue)' }}>{status.itemsCollected ?? 0}</strong>
                        <span style={{ color: 'var(--text-secondary)' }}> items</span>
                    </span>
                    <span>
                        <strong style={{ color: 'var(--accent-purple)' }}>{status.feedsProcessed ?? 0}</strong>
                        <span style={{ color: 'var(--text-secondary)' }}> feeds processed</span>
                    </span>
                    {status.status && (
                        <span className={`status-badge ${status.status === 'SUCCESS' ? 'status-active' : 'status-inactive'}`} style={{ marginLeft: 'auto' }}>
                            {status.status}
                        </span>
                    )}
                </div>
            )}

            {/* ─── Feed Cards ───────────────────────────────────── */}
            <div className="section-header">
                <span className="section-title">RSS Feed Sources <span className="section-count">{feeds.length}</span></span>
            </div>

            {loading ? (
                <div className="table-container">
                    <div className="loading-spinner"><div className="spinner" /><span>Loading feeds…</span></div>
                </div>
            ) : feeds.length === 0 ? (
                <div className="table-container">
                    <div className="empty-state">
                        <div className="empty-icon">📡</div>
                        <span>No feed sources configured yet</span>
                        <button className="topbar-btn btn-primary" style={{ marginTop: 10 }} onClick={() => setShowAdd(true)}>
                            + Add First Source
                        </button>
                    </div>
                </div>
            ) : (
                <div className="cards-grid">
                    {feeds.map(f => (
                        <div key={f.id} className="feed-card">
                            <div className="feed-card-header">
                                <div>
                                    <div className="feed-card-name">{f.name}</div>
                                    <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 2, fontFamily: 'JetBrains Mono, monospace' }}>
                                        {f.feedType || 'RSS'}
                                    </div>
                                </div>
                                <span className={`status-badge ${f.active ? 'status-active' : 'status-inactive'}`}>
                                    {f.active ? '● Active' : '○ Paused'}
                                </span>
                            </div>

                            <div className="feed-card-url">{f.url}</div>

                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <div className="feed-card-meta">
                                    {f.lastPulled && (
                                        <span>
                                            Last: <span style={{ color: 'var(--accent-cyan)', fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>
                                                {new Date(f.lastPulled).toLocaleDateString()}
                                            </span>
                                        </span>
                                    )}
                                    <span style={{ color: 'var(--text-muted)', fontSize: 11 }}>ID: {f.id}</span>
                                </div>
                                <button
                                    className={`feed-toggle-btn ${f.active ? 'deactivate' : 'activate'}`}
                                    onClick={() => handleToggle(f.id)}
                                >
                                    {f.active ? 'Pause' : 'Activate'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showAdd && <AddFeedModal onClose={() => setShowAdd(false)} onSave={handleAdd} />}
        </div>
    );
}
