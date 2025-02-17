INSERT INTO Scans (hostname, ip_address, mac_address, status, last_seen, custom_name) VALUES
('lab-pc-01', '192.168.1.10', 'AA:BB:CC:DD:EE:01', 1, NOW(), 'Main Lab PC'),
('lab-pc-02', '192.168.1.11', 'AA:BB:CC:DD:EE:02', 1, DATEADD('MINUTE', -5, NOW()), NULL),
('server-01', '192.168.1.50', 'AA:BB:CC:DD:EE:03', 0, DATEADD('HOUR', -2, NOW()), 'Research Server'),
('student-pc-01', '192.168.1.20', 'AA:BB:CC:DD:EE:04', 1, DATEADD('MINUTE', -10, NOW()), 'Library Workstation'),
('student-pc-02', '192.168.1.21', 'AA:BB:CC:DD:EE:05', 0, DATEADD('DAY', -1, NOW()), NULL),
('admin-laptop', '192.168.1.30', 'AA:BB:CC:DD:EE:06', 1, NOW(), 'Admin Laptop');


INSERT INTO OfflineEvents (device_id, offline_since, restored_at) VALUES
(3, DATEADD('HOUR', -2, NOW()), DATEADD('HOUR', -1, NOW())), -- Server went offline for 1 hour
(5, DATEADD('DAY', -1, NOW()), NULL), -- Student PC is still offline
(2, DATEADD('HOUR', -3, NOW()), DATEADD('HOUR', -2, NOW())), -- Lab PC was offline for 1 hour
(4, DATEADD('MINUTE', -30, NOW()), DATEADD('MINUTE', -10, NOW())), -- Library PC was offline for 20 minutes
(1, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -1, NOW())); -- Main Lab PC had a long downtime