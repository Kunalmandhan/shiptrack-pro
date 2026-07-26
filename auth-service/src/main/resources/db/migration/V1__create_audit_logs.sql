-- Auth Service: Flyway Migration V1
-- Creates the audit_logs table for authentication event tracking

CREATE SCHEMA IF NOT EXISTS shiptrack_auth;

CREATE TABLE shiptrack_auth.audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    action          VARCHAR(50)  NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       UUID,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    old_value       JSONB,
    new_value       JSONB,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes for common query patterns
CREATE INDEX idx_audit_user_id    ON shiptrack_auth.audit_logs (user_id);
CREATE INDEX idx_audit_action     ON shiptrack_auth.audit_logs (action);
CREATE INDEX idx_audit_created_at ON shiptrack_auth.audit_logs (created_at DESC);
CREATE INDEX idx_audit_entity     ON shiptrack_auth.audit_logs (entity_type, entity_id);
