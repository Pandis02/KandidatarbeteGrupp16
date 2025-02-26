-- TO RESET EVERYTHING
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
CREATE TABLE TrackedDevices (
    mac_address CHAR(17) PRIMARY KEY,
    custom_name VARCHAR(255)
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

-- NotificationEvents stores a log of all notifications ever sent from us
CREATE TABLE NotificationEvents (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mac_address CHAR(17) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- NotificationRecipients stores a log of recipients that the notification was sent to
CREATE TABLE NotificationRecipients (
    event_id BIGINT REFERENCES NotificationEvents(event_id) ON DELETE CASCADE,
    type VARCHAR(24) NOT NULL CHECK (type IN ('sms', 'email')),
    recipient VARCHAR(255) NOT NULL, -- can be an email like live@securitas.se or +46 123456789
    PRIMARY KEY (event_id, recipient)
);