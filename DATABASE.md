# 📄 Database Usage Guide: Device Monitoring System

This guide provides essential SQL operations for managing and retrieving data from the `Scans` and `OfflineEvents` tables.

---

## 1. Device Management

###  Inserts
```sql
INSERT INTO Scans (mac_address, hostname, ip_address, last_seen) 
VALUES ('00:1A:2B:3C:4D:5E', 'Device-01', '192.168.1.10', NOW());
```
Or
```sql
INSERT INTO Checkins (mac_address, last_checkin) 
VALUES ('00:1A:2B:3C:4D:5E', NOW());
```

> Keep in mind we will use something like a merge statement to do an upsert operation, since these rows are created once, and after that only edited on each update

### Updates
```sql
UPDATE Scans 
SET last_seen = NOW()
WHERE mac_address = '00:1A:2B:3C:4D:5E';
```
Or
```sql
UPDATE Checkins 
SET last_checkin = NOW()
WHERE mac_address = '00:1A:2B:3C:4D:5E';
```

## 2. Logging Offline Events

###  Mark a device going offline
```sql
INSERT INTO OfflineEvents (mac_address, offline_since) 
VALUES ('00:1A:2B:3C:4D:5E', NOW());
```

### Mark a device as restored
```sql
UPDATE OfflineEvents 
SET restored_at = NOW() 
WHERE event_id = (
    SELECT event_id FROM OfflineEvents
    WHERE mac_address = '00:1A:2B:3C:4D:5E' AND restored_at IS NULL
    LIMIT 1
);
```
But ideally the java part should maintain a list of `event_id`'s mapped to mac_addresses that are currenly offline and use that to update it. Or even fetch it right before restoring it.
```sql
UPDATE OfflineEvents 
SET restored_at = NOW() 
WHERE event_id = some_id;
```

## 3. Retrieving device & event information
###  Get all devices marked offline
```sql
SELECT * FROM OfflineEvents 
WHERE restored_at IS NULL
ORDER BY offline_since DESC;
```

###  Find devices offline for more than 5 minutes
```sql
SELECT * FROM OfflineEvents 
WHERE offline_since < NOW() - INTERVAL '5' MINUTE
ORDER BY offline_since DESC;
```