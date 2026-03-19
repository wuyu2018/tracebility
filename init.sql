-- Food Traceability System Database Initialization Script
-- Version: 1.0.0

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS food_traceability CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE food_traceability;

-- Create application user with limited privileges
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON food_traceability.* TO 'app_user'@'%';
FLUSH PRIVILEGES;

-- Table creation statements (if not using Hibernate auto DDL)
-- The actual schema is managed by JPA Hibernate, this file is for reference only

-- Note: For production deployment, ensure:
-- 1. Change all default passwords
-- 2. Use SSL/TLS for all connections
-- 3. Regular backup schedule
-- 4. Principle of least privilege for database users
