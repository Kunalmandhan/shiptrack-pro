-- Shipment Service: Flyway Migration V1
-- Creates shipments, shipment_status_history, drivers, vehicles, proof_of_delivery tables

CREATE SCHEMA IF NOT EXISTS shiptrack_shipment;

-- ==========================================
-- DRIVERS TABLE
-- ==========================================
CREATE TABLE shiptrack_shipment.drivers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)  NOT NULL,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    phone           VARCHAR(20)   NOT NULL,
    license_number  VARCHAR(50)   NOT NULL UNIQUE,
    available       BOOLEAN       NOT NULL DEFAULT true,
    current_lat     DOUBLE PRECISION,
    current_lng     DOUBLE PRECISION,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ==========================================
-- VEHICLES TABLE
-- ==========================================
CREATE TABLE shiptrack_shipment.vehicles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plate_number    VARCHAR(20)   NOT NULL UNIQUE,
    type            VARCHAR(20)   NOT NULL,
    model           VARCHAR(100)  NOT NULL,
    capacity_kg     DOUBLE PRECISION NOT NULL,
    available       BOOLEAN       NOT NULL DEFAULT true,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ==========================================
-- SHIPMENTS TABLE
-- ==========================================
CREATE TABLE shiptrack_shipment.shipments (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_number      VARCHAR(12)   NOT NULL UNIQUE,
    sender_id            UUID          NOT NULL,
    sender_name          VARCHAR(100)  NOT NULL,
    sender_email         VARCHAR(255)  NOT NULL,
    sender_phone         VARCHAR(20)   NOT NULL,
    origin_address       VARCHAR(500)  NOT NULL,
    origin_lat           DOUBLE PRECISION,
    origin_lng           DOUBLE PRECISION,
    receiver_id          UUID,
    receiver_name        VARCHAR(100)  NOT NULL,
    receiver_email       VARCHAR(255)  NOT NULL,
    receiver_phone       VARCHAR(20)   NOT NULL,
    destination_address  VARCHAR(500)  NOT NULL,
    destination_lat      DOUBLE PRECISION,
    destination_lng      DOUBLE PRECISION,
    status               VARCHAR(30)   NOT NULL DEFAULT 'CREATED',
    assigned_driver_id   UUID REFERENCES shiptrack_shipment.drivers(id),
    assigned_vehicle_id  UUID REFERENCES shiptrack_shipment.vehicles(id),
    weight_kg            DOUBLE PRECISION NOT NULL,
    dimensions           VARCHAR(50),
    package_type         VARCHAR(20)   NOT NULL,
    description          VARCHAR(1000),
    estimated_delivery   TIMESTAMP,
    actual_delivery      TIMESTAMP,
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ==========================================
-- SHIPMENT STATUS HISTORY TABLE
-- ==========================================
CREATE TABLE shiptrack_shipment.shipment_status_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id     UUID          NOT NULL REFERENCES shiptrack_shipment.shipments(id) ON DELETE CASCADE,
    from_status     VARCHAR(30),
    to_status       VARCHAR(30)   NOT NULL,
    changed_by      VARCHAR(100)  NOT NULL,
    notes           VARCHAR(500),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ==========================================
-- PROOF OF DELIVERY TABLE
-- ==========================================
CREATE TABLE shiptrack_shipment.proof_of_delivery (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id     UUID          NOT NULL UNIQUE REFERENCES shiptrack_shipment.shipments(id) ON DELETE CASCADE,
    signature_url   VARCHAR(500),
    photo_url       VARCHAR(500),
    received_by     VARCHAR(100)  NOT NULL,
    notes           VARCHAR(500),
    delivered_at    TIMESTAMP     NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ==========================================
-- INDEXES
-- ==========================================

-- Shipments
CREATE INDEX idx_shipments_tracking_number   ON shiptrack_shipment.shipments (tracking_number);
CREATE INDEX idx_shipments_sender_id         ON shiptrack_shipment.shipments (sender_id);
CREATE INDEX idx_shipments_status            ON shiptrack_shipment.shipments (status);
CREATE INDEX idx_shipments_assigned_driver    ON shiptrack_shipment.shipments (assigned_driver_id);
CREATE INDEX idx_shipments_created_at        ON shiptrack_shipment.shipments (created_at DESC);

-- Status History
CREATE INDEX idx_status_history_shipment     ON shiptrack_shipment.shipment_status_history (shipment_id);
CREATE INDEX idx_status_history_created_at   ON shiptrack_shipment.shipment_status_history (created_at);

-- Drivers
CREATE INDEX idx_drivers_available           ON shiptrack_shipment.drivers (available);
CREATE INDEX idx_drivers_email               ON shiptrack_shipment.drivers (email);

-- Vehicles
CREATE INDEX idx_vehicles_available          ON shiptrack_shipment.vehicles (available);
CREATE INDEX idx_vehicles_plate_number       ON shiptrack_shipment.vehicles (plate_number);
