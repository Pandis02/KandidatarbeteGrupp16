# 📄 Database Usage Guide: Device Monitoring System

This guide provides essential SQL operations for managing and retrieving data from the `Scans` and `OfflineEvents` tables.

---

## **1. Device Management**

###  Insert a New Device
```sql
INSERT INTO Scans (hostname, ip_address, mac_address, status, last_seen) 
VALUES ('Device-01', '192.168.1.10', '00:1A:2B:3C:4D:5E', 1, NOW());
```

### Update Device Status
```sql
UPDATE Scans 
SET last_seen = NOW(), status = 1 
WHERE mac_address = '00:1A:2B:3C:4D:5E';
```

### Mark Device as Offline 
```sql
UPDATE Scans 
SET status = 0 
WHERE last_seen < DATEADD('MINUTE', -5, NOW()); 

```

## **2. Logging Offline Events**

###  Log a Device Going Offline
```sql
INSERT INTO OfflineEvents (device_id, offline_since) 
SELECT device_id, NOW() 
FROM Scans 
WHERE status = 0;

```

### Mark a Device as Restored (Back Online)
```sql
UPDATE OfflineEvents 
SET restored_at = NOW() 
WHERE device_id = (
    SELECT device_id FROM Scans WHERE mac_address = '00:1A:2B:3C:4D:5E'
) 
AND restored_at IS NULL;

```
## **3. Retrieving Device & Event Information**
###  Get All Active Devices
```sql
SELECT * FROM Scans WHERE status = 1;
```

###  Find Devices Offline for More Than 5 Minutes (red)
```sql
SELECT hostname, ip_address, last_seen 
FROM Scans 
WHERE status = 0 
AND last_seen < DATEADD('MINUTE', -5, NOW());
```

###  Find Devices Offline for LESS Than 5 Minutes (yellow)
```sql
SELECT hostname, ip_address, last_seen 
FROM Scans 
WHERE status = 0 
AND last_seen > DATEADD('MINUTE', -5, NOW());
```

###  List Offline Event History
```sql
SELECT s.hostname, e.offline_since, e.restored_at 
FROM OfflineEvents e 
JOIN Scans s ON e.device_id = s.device_id;
```




