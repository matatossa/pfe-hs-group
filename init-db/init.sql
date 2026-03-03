-- Security Platform Database Schema
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'ANALYST',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active     BOOLEAN      NOT NULL DEFAULT TRUE
);

-- FEED SOURCES
CREATE TABLE IF NOT EXISTS feed_sources (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    url         VARCHAR(500) NOT NULL UNIQUE,
    feed_type   VARCHAR(50)  NOT NULL DEFAULT 'RSS',
    last_pulled TIMESTAMP,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- VULNERABILITIES (normalized)
CREATE TABLE IF NOT EXISTS vulnerabilities (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source          VARCHAR(100),
    product         VARCHAR(255),
    cve_id          VARCHAR(100),
    title           TEXT          NOT NULL,
    description     TEXT,
    severity        VARCHAR(50),
    cvss_score      DECIMAL(4,1),
    risk_score      DECIMAL(5,2),
    url             VARCHAR(1000),
    published_at    TIMESTAMP,
    fetched_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    feed_source_id  INTEGER REFERENCES feed_sources(id),
    is_relevant     BOOLEAN       NOT NULL DEFAULT FALSE,
    relevance_score DECIMAL(4,3),
    raw_data        TEXT
);

-- ALERTS
CREATE TABLE IF NOT EXISTS alerts (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vulnerability_id  UUID REFERENCES vulnerabilities(id) ON DELETE CASCADE,
    severity_level    VARCHAR(50) NOT NULL,
    message           TEXT,
    triggered_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged      BOOLEAN     NOT NULL DEFAULT FALSE,
    acknowledged_by   UUID REFERENCES users(id),
    acknowledged_at   TIMESTAMP
);

-- RISK SCORES HISTORY
CREATE TABLE IF NOT EXISTS risk_score_history (
    id               SERIAL PRIMARY KEY,
    vulnerability_id UUID REFERENCES vulnerabilities(id) ON DELETE CASCADE,
    risk_score       DECIMAL(5,2),
    cvss_component   DECIMAL(4,1),
    business_impact  DECIMAL(3,1),
    exposure_factor  DECIMAL(3,1),
    threat_weight    DECIMAL(3,1),
    calculated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- DEFAULT SEED DATA

-- Feed Sources
INSERT INTO feed_sources (name, url, feed_type) VALUES
  ('CERT-MU', 'https://www.cert-mu.govmu.org/feed/', 'RSS'),
  ('Cyber.gc.ca', 'https://www.cyber.gc.ca/api/atip/rss-feed/alerts-advisories', 'RSS'),
  ('DGSSI', 'https://www.dgssi.gov.ma/fr/bulletins-de-securite.html', 'HTML')
ON CONFLICT (url) DO NOTHING;

-- Default admin user (password: Admin@1234 - bcrypt hashed)
INSERT INTO users (username, email, password, role) VALUES
  ('admin', 'admin@security-platform.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_vuln_product     ON vulnerabilities (product);
CREATE INDEX IF NOT EXISTS idx_vuln_severity    ON vulnerabilities (severity);
CREATE INDEX IF NOT EXISTS idx_vuln_cve         ON vulnerabilities (cve_id);
CREATE INDEX IF NOT EXISTS idx_vuln_published   ON vulnerabilities (published_at DESC);
CREATE INDEX IF NOT EXISTS idx_vuln_risk_score  ON vulnerabilities (risk_score DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_severity  ON alerts (severity_level);
CREATE INDEX IF NOT EXISTS idx_alerts_triggered ON alerts (triggered_at DESC);
