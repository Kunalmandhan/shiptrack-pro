-- Create Analytics Service database schema
CREATE SCHEMA IF NOT EXISTS shiptrack_analytics;

-- Table: report_exports
CREATE TABLE IF NOT EXISTS shiptrack_analytics.report_exports (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    parameters TEXT,
    status VARCHAR(20) NOT NULL,
    download_url VARCHAR(500),
    generated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: daily_metrics_snapshots
CREATE TABLE IF NOT EXISTS shiptrack_analytics.daily_metrics_snapshots (
    id UUID PRIMARY KEY,
    snapshot_date DATE UNIQUE NOT NULL,
    total_shipments BIGINT NOT NULL DEFAULT 0,
    delivered_count BIGINT NOT NULL DEFAULT 0,
    delayed_count BIGINT NOT NULL DEFAULT 0,
    cancelled_count BIGINT NOT NULL DEFAULT 0,
    on_time_delivery_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    avg_delivery_hours DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_reports_user ON shiptrack_analytics.report_exports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_status ON shiptrack_analytics.report_exports(status);
CREATE INDEX IF NOT EXISTS idx_snapshots_date ON shiptrack_analytics.daily_metrics_snapshots(snapshot_date);
