import { useState, useEffect } from 'react';
import { rssApi } from '../api.js';

export default function RawEntriesPage({ toast }) {
    const [feeds, setFeeds] = useState([]);
    const [entries, setEntries] = useState([]);
    const [unprocessed, setUnproc] = useState([]);
    const [selectedFeed, setSelectedFeed] = useState(null);
    const [tab, setTab] = useState('all');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadFeeds();
        loadUnprocessed();
    }, []);

    async function loadFeeds() {
        try {
            setFeeds(await rssApi.getFeeds() || []);
        } catch (e) {
            toast('Failed to load feeds', 'error');
        }
    }

    async function loadUnprocessed() {
        setLoading(true);
        try {
            setUnproc(await rssApi.getUnprocessed() || []);
        } finally {
            setLoading(false);
        }
    }

    async function handleFeedSelect(id) {
        setSelectedFeed(id);
        setTab('feed');
        setLoading(true);
        try {
            setEntries(await rssApi.getFeedEntries(id) || []);
        } catch (e) {
            toast('Failed to load entries', 'error');
        } finally {
            setLoading(false);
        }
    }

    const displayed = tab === 'unprocessed' ? unprocessed : tab === 'feed' ? entries : unprocessed;

    return (
        <div>
            {/* ─── Tabs ────────────────────────────────────────── */}
            <div style={{ display: 'flex', gap: 12, marginBottom: 20, alignItems: 'center' }}>
                <div className="tabs">
                    <button className={`tab ${tab === 'all' ? 'active' : ''}`} onClick={() => { setTab('all'); setSelectedFeed(null); }}>
                        Unprocessed ({unprocessed.length})
                    </button>
                    <button
                        className={`tab ${tab === 'feed' ? 'active' : ''}`}
                        disabled={!selectedFeed}
                    >
                        {selectedFeed
                            ? `Feed #${selectedFeed} (${entries.length})`
                            : 'Select a feed →'}
                    </button>
                </div>
            </div>

            <div className="two-col">
                {/* ─── Feed picker ─────────────────────────────────── */}
                <div>
                    <div className="section-header">
                        <span className="section-title">📡 Feed Sources</span>
                    </div>
                    <div className="table-container">
                        {feeds.length === 0 ? (
                            <div className="empty-state"><div className="empty-icon">📡</div>No feeds</div>
                        ) : (
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Type</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {feeds.map(f => (
                                        <tr
                                            key={f.id}
                                            style={{ cursor: 'pointer', background: selectedFeed === f.id ? 'rgba(88,166,255,0.07)' : undefined }}
                                            onClick={() => handleFeedSelect(f.id)}
                                        >
                                            <td style={{ fontWeight: selectedFeed === f.id ? 600 : 400 }}>{f.name}</td>
                                            <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{f.feedType}</td>
                                            <td>
                                                <span className={`status-badge ${f.active ? 'status-active' : 'status-inactive'}`}>
                                                    {f.active ? 'Active' : 'Paused'}
                                                </span>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>

                {/* ─── Entry list ─────────────────────────────────── */}
                <div>
                    <div className="section-header">
                        <span className="section-title">
                            {tab === 'feed' ? `Entries for Feed #${selectedFeed}` : 'Unprocessed Entries'}
                            <span className="section-count">{displayed.length}</span>
                        </span>
                    </div>
                    <div className="table-container" style={{ maxHeight: 520, overflowY: 'auto' }}>
                        {loading ? (
                            <div className="loading-spinner"><div className="spinner" /></div>
                        ) : displayed.length === 0 ? (
                            <div className="empty-state">
                                <div className="empty-icon">📄</div>
                                <span>{tab === 'feed' ? 'No entries for this feed' : 'No unprocessed entries — all caught up!'}</span>
                            </div>
                        ) : (
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Title</th>
                                        <th>Processed</th>
                                        <th>Fetched At</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {displayed.map(e => (
                                        <tr key={e.id}>
                                            <td>
                                                {e.link ? (
                                                    <a
                                                        href={e.link}
                                                        target="_blank"
                                                        rel="noreferrer"
                                                        style={{ color: 'var(--accent-blue)', textDecoration: 'none', fontSize: 13 }}
                                                        onClick={ev => ev.stopPropagation()}
                                                    >
                                                        <div style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                            {e.title || e.link}
                                                        </div>
                                                    </a>
                                                ) : (
                                                    <div style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontSize: 13 }}>
                                                        {e.title || '(no title)'}
                                                    </div>
                                                )}
                                            </td>
                                            <td>
                                                {e.processed
                                                    ? <span style={{ color: 'var(--accent-green)', fontSize: 13 }}>✓</span>
                                                    : <span style={{ color: 'var(--severity-medium)', fontSize: 13 }}>⏳</span>}
                                            </td>
                                            <td style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'JetBrains Mono, monospace' }}>
                                                {e.fetchedAt ? new Date(e.fetchedAt).toLocaleString() : '—'}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
