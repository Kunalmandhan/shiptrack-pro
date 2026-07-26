-- Notification Service: Flyway Migration V1
-- Creates notifications table

CREATE SCHEMA IF NOT EXISTS shiptrack_notification;

-- ==========================================
-- NOTIFICATIONS TABLE
-- ==========================================
CREATE TABLE shiptrack_notification.notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id    UUID,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(20),
    channel         VARCHAR(20)   NOT NULL,
    type            VARCHAR(30)   NOT NULL,
    title           VARCHAR(200)  NOT NULL,
    message         TEXT          NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    is_read         BOOLEAN       NOT NULL DEFAULT false,
    reference_id    UUID,
    read_at         TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ==========================================
-- INDEXES
-- ==========================================
CREATE INDEX idx_notif_recipient_id   ON shiptrack_notification.notifications (recipient_id);
CREATE INDEX idx_notif_channel        ON shiptrack_notification.notifications (channel);
CREATE INDEX idx_notif_status         ON shiptrack_notification.notifications (status);
CREATE INDEX idx_notif_is_read        ON shiptrack_notification.notifications (is_read);
CREATE INDEX idx_notif_created_at     ON shiptrack_notification.notifications (created_at DESC);
