-- TO RESET EVERYTHING
DROP TABLE IF EXISTS Scans CASCADE;
DROP TABLE IF EXISTS OfflineEvents CASCADE;
DROP TABLE IF EXISTS AlertTypes CASCADE;
DROP TABLE IF EXISTS CheckIns CASCADE;

-- CREATE TABLES
CREATE TABLE Scans (
    device_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostname VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    mac_address VARCHAR(17) UNIQUE NOT NULL,
    status SMALLINT DEFAULT 1 CHECK (status IN (0,1)), -- 0 = offline, 1= online
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    custom_name VARCHAR(255)
);

CREATE TABLE OfflineEvents (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES Scans(device_id) ON DELETE CASCADE,
    offline_since TIMESTAMP NOT NULL,
    restored_at TIMESTAMP NULL,
    CONSTRAINT chk_timestamps CHECK (restored_at IS NULL OR restored_at > offline_since)
);

CREATE TABLE AlertTypes (
    type_id INT PRIMARY KEY,
    type_name VARCHAR(10) UNIQUE NOT NULL
);

CREATE TABLE Checkins (
    mac_address CHAR(17) PRIMARY KEY,
    last_checkin TIMESTAMP NOT NULL
);

-- Insert the alert types 
INSERT INTO AlertTypes (type_id, type_name) VALUES
(1, 'email'),
(2, 'SMS'),
(3, 'push');

-- Notificaiton table stores the event itself
CREATE TABLE Notifications (
    alert_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES Scans(device_id) ON DELETE CASCADE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- NotificationRecipient table stores where and how the alert was sent
CREATE TABLE NotificationRecipients (
    recipient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id BIGINT REFERENCES Notifications(alert_id) ON DELETE CASCADE,
    type_id INT REFERENCES AlertTypes(type_id) ON DELETE CASCADE,
    recipient VARCHAR(255) NOT NULL
);