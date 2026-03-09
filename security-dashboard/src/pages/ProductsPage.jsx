import { useState, useEffect, useCallback, useRef } from 'react';
import { productsApi } from '../api.js';

export default function ProductsPage({ toast }) {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [newName, setNewName] = useState('');
    const [search, setSearch] = useState('');
    const [adding, setAdding] = useState(false);

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const data = await productsApi.getAll();
            setProducts(data || []);
        } catch (e) {
            toast('Failed to load products: ' + e.message, 'error');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { load(); }, [load]);

    async function handleAdd(e) {
        e.preventDefault();
        if (!newName.trim()) return;
        setAdding(true);
        try {
            await productsApi.add(newName.trim());
            setNewName('');
            await load();
            toast(`Added "${newName.trim()}" to monitored products`, 'success');
        } catch (err) {
            toast('Failed to add product: ' + err.message, 'error');
        } finally {
            setAdding(false);
        }
    }

    async function handleRemove(p) {
        if (!confirm(`Remove "${p.name}" from monitored products?\n\nExisting vulnerabilities are kept, but future ones for this product will not be marked as relevant.`)) return;
        try {
            await productsApi.remove(p.id);
            await load();
            toast(`Removed "${p.name}"`, 'success');
        } catch (err) {
            toast('Failed to remove: ' + err.message, 'error');
        }
    }

    async function handleToggle(p) {
        try {
            await productsApi.toggle(p.id);
            await load();
            toast(`${p.name} is now ${p.active ? 'disabled' : 'enabled'}`, 'info');
        } catch (err) {
            toast('Failed to toggle: ' + err.message, 'error');
        }
    }

    const active = products.filter(p => p.active);
    const inactive = products.filter(p => !p.active);

    // Sort so active are first, then alphabetical
    const sortedProducts = [...products].sort((a, b) => {
        if (a.active !== b.active) return a.active ? -1 : 1;
        return a.name.localeCompare(b.name);
    });

    // Apply local search filter
    const displayedProducts = sortedProducts.filter(p =>
        !search.trim() || p.name.toLowerCase().includes(search.trim().toLowerCase())
    );

    return (
        <div>
            {/* ─── Stats ─── */}
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 24 }}>
                <div className="stat-card blue">
                    <div className="stat-header"><span className="stat-label">Total Products</span><div className="stat-icon">🏷️</div></div>
                    <div className="stat-value">{products.length}</div>
                </div>
                <div className="stat-card green">
                    <div className="stat-header"><span className="stat-label">Active</span><div className="stat-icon">✅</div></div>
                    <div className="stat-value">{active.length}</div>
                </div>
                <div className="stat-card orange">
                    <div className="stat-header"><span className="stat-label">Disabled</span><div className="stat-icon">⏸️</div></div>
                    <div className="stat-value">{inactive.length}</div>
                </div>
            </div>

            {/* ─── Add Product ─── */}
            <div className="section-header" style={{ marginBottom: 12 }}>
                <span className="section-title">Add Product to Monitor</span>
            </div>
            <form onSubmit={handleAdd} style={{ display: 'flex', gap: 10, marginBottom: 32, alignItems: 'center', flexWrap: 'wrap' }}>
                <div className="search-bar" style={{ flex: '1 1 280px', maxWidth: 400 }}>
                    <span className="search-icon">🏷️</span>
                    <input
                        className="search-input"
                        placeholder="e.g. Ubuntu, Google Chrome, Cisco…"
                        value={newName}
                        onChange={e => setNewName(e.target.value)}
                        disabled={adding}
                    />
                </div>
                <button
                    type="submit"
                    className="topbar-btn btn-primary"
                    disabled={adding || !newName.trim()}
                    style={{
                        background: 'var(--accent-blue)', color: '#fff', border: 'none',
                        padding: '8px 20px', borderRadius: 8, fontWeight: 600, cursor: 'pointer',
                        opacity: (adding || !newName.trim()) ? 0.5 : 1,
                    }}
                >
                    {adding ? '…' : '+ Add'}
                </button>
                <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                    ⚠️ Name must match a known product keyword (e.g. "Ubuntu", "Windows", "Cisco").
                </span>
            </form>

            {/* ─── Product List Table ─── */}
            <div className="section-header" style={{ marginBottom: 16 }}>
                <span className="section-title">
                    Monitored Products List <span className="section-count">{products.length}</span>
                </span>
                <div style={{ marginLeft: 'auto', display: 'flex', gap: 10 }}>
                    <div className="search-bar" style={{ width: 260 }}>
                        <span className="search-icon">🔍</span>
                        <input
                            className="search-input"
                            placeholder="Find product to remove/toggle…"
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                        />
                    </div>
                </div>
            </div>

            <div className="table-container">
                {loading ? (
                    <div className="loading-spinner"><div className="spinner" /><span>Loading products…</span></div>
                ) : displayedProducts.length === 0 ? (
                    <div className="empty-state">
                        <div className="empty-icon">🔍</div>
                        {search ? 'No products match your search.' : 'No monitored products found.'}
                    </div>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Status</th>
                                <th>Product Name</th>
                                <th>Date Added</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {displayedProducts.map(p => (
                                <tr key={p.id} style={{ opacity: p.active ? 1 : 0.6 }}>
                                    <td style={{ width: 100 }}>
                                        {p.active
                                            ? <span className="status-badge status-relevant">✅ Active</span>
                                            : <span className="status-badge" style={{ background: 'var(--bg-tertiary)', color: 'var(--text-secondary)' }}>⏸️ Disabled</span>}
                                    </td>
                                    <td style={{ fontWeight: 600, color: p.active ? 'var(--text-primary)' : 'var(--text-secondary)' }}>
                                        {p.name}
                                    </td>
                                    <td style={{ fontSize: 13, color: 'var(--text-muted)' }}>
                                        {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '—'}
                                    </td>
                                    <td style={{ textAlign: 'right' }}>
                                        <button
                                            className="topbar-btn btn-ghost"
                                            onClick={() => handleToggle(p)}
                                            style={{ marginRight: 8, padding: '4px 10px', fontSize: 13, color: p.active ? 'var(--severity-medium)' : 'var(--accent-green)' }}
                                        >
                                            {p.active ? '⏸️ Disable' : '▶️ Enable'}
                                        </button>
                                        <button
                                            className="topbar-btn btn-ghost"
                                            onClick={() => handleRemove(p)}
                                            style={{ padding: '4px 10px', fontSize: 13, color: 'var(--severity-high)' }}
                                            title="Permanently Delete"
                                        >
                                            ✕ Remove
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            {/* ─── Info box ─── */}
            <div style={{
                marginTop: 40, background: 'var(--bg-card)', border: '1px solid var(--border-subtle)',
                borderRadius: 10, padding: '16px 20px', fontSize: 13, color: 'var(--text-secondary)', lineHeight: 1.7,
            }}>
                <strong style={{ color: 'var(--text-primary)' }}>ℹ️ How this works</strong><br />
                This list controls what vulnerabilities are marked as <strong>Relevant</strong> in the system.
                When the normalization service processes a new advisory, it checks if the detected product is in this list.
                Adding a product affects <em>future</em> advisories. Existing vulnerabilities keep their current status.
            </div>
        </div>
    );
}
