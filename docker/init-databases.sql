-- PostgreSQL initialization script
-- Creates all 6 databases (one per microservice schema)
-- Runs automatically when the PostgreSQL container starts for the first time

CREATE DATABASE shiptrack_auth;
CREATE DATABASE shiptrack_user;
CREATE DATABASE shiptrack_shipment;
CREATE DATABASE shiptrack_tracking;
CREATE DATABASE shiptrack_notification;
CREATE DATABASE shiptrack_analytics;
