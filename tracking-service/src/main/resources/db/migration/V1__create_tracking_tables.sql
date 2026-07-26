-- Tracking Service: Flyway Migration V1
-- Creates tracking_history table

CREATE SCHEMA IF NOT EXISTS shiptrack_tracking;

-- ==========================================
-- TRACKING HISTORY TABLE
-- ==========================================
CREATE TABLE shiptrack_tracking.tracking_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id     UUID          NOT NULL,
    driver_id       UUID,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    speed_kmh       DOUBLE PRECISION,
    heading_degrees DOUBLE PRECISION,
    altitude_m      DOUBLE PRECISION,
    recorded_at     TIMESTAMP     NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ==========================================
-- INDEXES
-- ==========================================
CREATE INDEX idx_tracking_shipment_id ON shiptrack_tracking.tracking_history (shipment_id);
CREATE INDEX idx_tracking_driver_id   ON shiptrack_tracking.tracking_history (driver_id);
CREATE INDEX idx_tracking_recorded_at  ON shiptrack_tracking.tracking_history (recorded_at DESC);
CREATE INDEX idx_tracking_shipment_time ON shiptrack_tracking.tracking_history (shipment_id, recorded_at DESC);
