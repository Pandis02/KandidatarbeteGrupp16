INSERT INTO Scans (hostname, ip_address, mac_address, last_seen) VALUES
('lab-pc-01', '192.168.1.10', 'AA:BB:CC:DD:EE:01', NOW()),
('lab-pc-02', '192.168.1.11', 'AA:BB:CC:DD:EE:02', DATEADD('MINUTE', -5, NOW())),
('server-01', '192.168.1.50', 'AA:BB:CC:DD:EE:03', DATEADD('HOUR', -2, NOW())),
('student-pc-01', '192.168.1.20', 'AA:BB:CC:DD:EE:04', DATEADD('MINUTE', -10, NOW())),
('student-pc-02', '192.168.1.21', 'AA:BB:CC:DD:EE:05', DATEADD('DAY', -1, NOW())),
('admin-laptop', '192.168.1.30', 'AA:BB:CC:DD:EE:06', NOW());


INSERT INTO OfflineEvents (mac_address, offline_since, restored_at) VALUES
('AA:BB:CC:DD:EE:01', DATEADD('HOUR', -2, NOW()), DATEADD('HOUR', -1, NOW())), -- Server went offline for 1 hour
('AA:BB:CC:DD:EE:02', DATEADD('DAY', -1, NOW()), NULL), -- Student PC is still offline
('AA:BB:CC:DD:EE:03', DATEADD('HOUR', -3, NOW()), DATEADD('HOUR', -2, NOW())), -- Lab PC was offline for 1 hour
('AA:BB:CC:DD:EE:05', DATEADD('MINUTE', -30, NOW()), DATEADD('MINUTE', -10, NOW())), -- Library PC was offline for 20 minutes
('AA:BB:CC:DD:EE:06', DATEADD('DAY', -2, NOW()), DATEADD('DAY', -1, NOW())); -- Main Lab PC had a long downtime

BEGIN TRANSACTION;

-- Step 1: Insert notification event
INSERT INTO NotificationEvents (mac_address, message)
VALUES ('AA:BB:CC:DD:EE:01', 'Device with mac AA:BB:CC:DD:EE:01 has been offline for 5 minutes!');

-- Step 2: Retrieve the last inserted event_id
-- H2 does not support session variables, so we use a subquery in the next INSERT
INSERT INTO NotificationRecipients (event_id, type, recipient)
VALUES
    ((SELECT MAX(event_id) FROM NotificationEvents), 'email', 'admin@example.com'),
    ((SELECT MAX(event_id) FROM NotificationEvents), 'sms', '0707123456');

COMMIT;