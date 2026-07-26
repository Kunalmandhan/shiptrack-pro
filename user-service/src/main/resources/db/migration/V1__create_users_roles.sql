-- User Service: Flyway Migration V1
-- Creates users, roles, and user_roles tables

CREATE SCHEMA IF NOT EXISTS shiptrack_user;

-- ==========================================
-- ROLES TABLE
-- ==========================================
CREATE TABLE shiptrack_user.roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50)  NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ==========================================
-- USERS TABLE
-- ==========================================
CREATE TABLE shiptrack_user.users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255),
    phone               VARCHAR(20),
    avatar_url          VARCHAR(500),
    auth_provider       VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id         VARCHAR(255),
    enabled             BOOLEAN      NOT NULL DEFAULT false,
    email_verified      BOOLEAN      NOT NULL DEFAULT false,
    account_non_locked  BOOLEAN      NOT NULL DEFAULT true,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ==========================================
-- USER_ROLES JOIN TABLE
-- ==========================================
CREATE TABLE shiptrack_user.user_roles (
    user_id  UUID NOT NULL REFERENCES shiptrack_user.users(id) ON DELETE CASCADE,
    role_id  UUID NOT NULL REFERENCES shiptrack_user.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ==========================================
-- INDEXES
-- ==========================================
CREATE INDEX idx_users_email          ON shiptrack_user.users (email);
CREATE INDEX idx_users_auth_provider  ON shiptrack_user.users (auth_provider);
CREATE INDEX idx_users_enabled        ON shiptrack_user.users (enabled);
CREATE INDEX idx_users_created_at     ON shiptrack_user.users (created_at DESC);

-- ==========================================
-- SEED DATA: Default roles
-- ==========================================
INSERT INTO shiptrack_user.roles (name) VALUES ('ADMIN'), ('CUSTOMER');
