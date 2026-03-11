const RSS_BASE = 'http://localhost:8081/api';
const NORM_BASE = 'http://localhost:8082/api';

async function request(url, options = {}) {
    try {
        const res = await fetch(url, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } });
        if (!res.ok) throw new Error(`HTTP ${res.status} – ${res.statusText}`);
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    } catch (err) {
        console.error('API Error:', url, err);
        throw err;
    }
}

// ─── RSS Collector Service ───────────────────────────────────────
export const rssApi = {
    /** GET /api/collect/status */
    getStatus: () => request(`${RSS_BASE}/collect/status`),
    /** POST /api/collect/trigger */
    trigger: () => request(`${RSS_BASE}/collect/trigger`, { method: 'POST' }),
    /** GET /api/feeds */
    getFeeds: () => request(`${RSS_BASE}/feeds`),
    /** POST /api/feeds */
    addFeed: (feedSource) => request(`${RSS_BASE}/feeds`, { method: 'POST', body: JSON.stringify(feedSource) }),
    /** PUT /api/feeds/{id}/toggle */
    toggleFeed: (id) => request(`${RSS_BASE}/feeds/${id}/toggle`, { method: 'PUT' }),
    /** GET /api/feeds/{id}/entries */
    getFeedEntries: (id) => request(`${RSS_BASE}/feeds/${id}/entries`),
    /** GET /api/entries/unprocessed */
    getUnprocessed: () => request(`${RSS_BASE}/entries/unprocessed`),
    /** GET /api/entries/stats */
    getEntryStats: () => request(`${RSS_BASE}/entries/stats`),
};

// ─── Normalization Service ───────────────────────────────────────
export const normApi = {
    /** GET /api/vulnerabilities */
    getAll: () => request(`${NORM_BASE}/vulnerabilities`),
    /** GET /api/vulnerabilities/relevant */
    getRelevant: () => request(`${NORM_BASE}/vulnerabilities/relevant`),
    /** GET /api/vulnerabilities/{id} */
    getById: (id) => request(`${NORM_BASE}/vulnerabilities/${id}`),
    /** GET /api/vulnerabilities/search?product=xxx */
    searchByProduct: (product) => request(`${NORM_BASE}/vulnerabilities/search?product=${encodeURIComponent(product)}`),
    /** GET /api/vulnerabilities/severity/{level} */
    getBySeverity: (level) => request(`${NORM_BASE}/vulnerabilities/severity/${level}`),
    /** GET /api/vulnerabilities/cve/{cveId} */
    getByCve: (cveId) => request(`${NORM_BASE}/vulnerabilities/cve/${cveId}`),
    /** GET /api/vulnerabilities/stats */
    getStats: () => request(`${NORM_BASE}/vulnerabilities/stats`),
};

// ─── Monitored Products ──────────────────────────────────────────
export const productsApi = {
    /** GET /api/products */
    getAll: () => request(`${NORM_BASE}/products`),
    /** POST /api/products — body: { name, version } */
    add: (name, version) => request(`${NORM_BASE}/products`, { method: 'POST', body: JSON.stringify({ name, version }) }),
    /** PUT /api/products/{id} — update (currently only version) */
    update: (id, updates) => request(`${NORM_BASE}/products/${id}`, { method: 'PUT', body: JSON.stringify(updates) }),
    /** DELETE /api/products/{id} */
    remove: (id) => request(`${NORM_BASE}/products/${id}`, { method: 'DELETE' }),
    /** PUT /api/products/{id}/toggle */
    toggle: (id) => request(`${NORM_BASE}/products/${id}/toggle`, { method: 'PUT' }),
};

// ─── Health check ────────────────────────────────────────────────
export async function checkHealth() {
    const results = { rss: false, norm: false };
    try {
        await fetch(`${RSS_BASE}/entries/stats`);
        results.rss = true;
    } catch { }
    try {
        await fetch(`${NORM_BASE}/vulnerabilities/stats`);
        results.norm = true;
    } catch { }
    return results;
}
