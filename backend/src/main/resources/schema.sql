-- TO RESET EVERYTHING
DROP TABLE IF EXISTS AdminSettings CASCADE;
DROP TABLE IF EXISTS Locations CASCADE;
DROP TABLE IF EXISTS TrackedDevices CASCADE;
DROP TABLE IF EXISTS Scans CASCADE;
DROP TABLE IF EXISTS OfflineEvents CASCADE;
DROP TABLE IF EXISTS AlertTypes CASCADE;
DROP TABLE IF EXISTS Checkins CASCADE;
DROP TABLE IF EXISTS NotificationEvents CASCADE;
DROP TABLE IF EXISTS NotificationRecipients CASCADE;

-- CREATE TABLES

-- TrackedDevices is a list of devices for which we WANT notifications, this will be managed via a UI and not manually
/* 
Imo this is how I think the app should work
Scans & Checkins is used for device discovery, then we display all available devices in a nice UI where the
administrators can click a check button beside a device that will add that device to tracked, meaning we have to
send notifications for it, if and when it goes silent.
This also makes it easy for administrators to safely shutdown or expire lab devices without us flagging them.
The mac_address'es are not FOREIGN KEYS for a reason, because each source (scans, checkins) work independently
and OfflineEvents & NotificationEvents should retain their rows even if a device is removed from Scans or Checkins or TrackedDevices
Foreign keys are for when you know your data cannot exist on their own but require a corresponding row in another table,
Here we actually do want the unrelatability of independent primary keys, logs for one should always be retained
*/

CREATE TABLE AdminSettings (
    id INT PRIMARY KEY CHECK (id = 1), -- Forces a single row
    alert_threshold_minutes INT NOT NULL CHECK (alert_threshold_minutes > 0),
    checkin_interval_seconds INT NOT NULL CHECK (checkin_interval_seconds > 0),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE Locations (
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building VARCHAR(255) NOT NULL,
    room VARCHAR(255) NOT NULL,
    UNIQUE (building, room) -- Ensuring combination of room and building is unique
);

CREATE TABLE TrackedDevices (
    mac_address CHAR(17) PRIMARY KEY,
    custom_name VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    location_id BIGINT REFERENCES Locations(location_id) ON DELETE SET NULL
);

CREATE TABLE Scans (
    mac_address CHAR(17) PRIMARY KEY,
    hostname VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Checkins (
    mac_address CHAR(17) PRIMARY KEY,
    last_checkin TIMESTAMP NOT NULL
);

CREATE TABLE OfflineEvents (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mac_address CHAR(17) NOT NULL,
    offline_since TIMESTAMP NOT NULL,
    restored_at TIMESTAMP,
    CONSTRAINT chk_timestamps CHECK (restored_at IS NULL OR restored_at > offline_since)
);

CREATE TABLE NotificationTriggers (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mac_address CHAR(17) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

CREATE TABLE Recipients (
    recipient_id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- Unique ID for recipient
    recipient_type VARCHAR(20) NOT NULL CHECK (recipient_type IN ('email', 'sms', 'push')), 
    recipient_value VARCHAR(255) NOT NULL,  -- Holds either email, phone number, or push token
    CHECK (
        (recipient_type = 'email' AND recipient_value REGEXP '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$') OR -- Email regex check
        (recipient_type = 'sms' AND recipient_value REGEXP '^\+?[1-9]\d{1,14}$') OR -- E.164 format phone number with optional + sign
        (recipient_type = 'push' AND LENGTH(recipient_value) = 153) -- Change to length of push depending on service. 153 is for Firebase
    )
);

CREATE TABLE RecipientRoles (
    recipient_id BIGINT,
    role_id INT,
    PRIMARY KEY (recipient_id, role_id),
    FOREIGN KEY (recipient_id) REFERENCES Recipients(recipient_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);

CREATE TABLE NotificationRecipientEvents (
    event_id BIGINT,
    recipient_id BIGINT,
    PRIMARY KEY (event_id, recipient_id),
    FOREIGN KEY (event_id) REFERENCES NotificationTriggers(event_id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES Recipients(recipient_id) ON DELETE CASCADE
);

-- Hard coded mock data
INSERT INTO Locations (building, room) VALUES
('EDIT', 'A1'),
('EDIT', 'A2'),
('SB', 'BX'),
('SB', 'BY'),
('SB', 'BZ');

INSERT INTO AdminSettings (id, alert_threshold_minutes, checkin_interval_seconds)
VALUES (1, 3, 15);