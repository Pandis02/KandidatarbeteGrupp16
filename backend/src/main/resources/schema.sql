-- TO RESET EVERYTHING
DROP TABLE IF EXISTS Scans CASCADE;
DROP TABLE IF EXISTS OfflineEvents CASCADE;

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