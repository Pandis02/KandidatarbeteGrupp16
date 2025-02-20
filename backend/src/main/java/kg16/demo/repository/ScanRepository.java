package kg16.demo.repository;

import kg16.demo.model.Scan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ScanRepository extends JpaRepository<Scan, Long> {

    Optional<Scan> findByMacAddress(String macAddress);

    @Modifying
    @Transactional
    @Query("UPDATE Scan s SET s.ipAddress = :ip, s.hostname = :hostname, s.status = :status, s.lastSeen = CURRENT_TIMESTAMP WHERE s.macAddress = :mac")
    int updateScan(@Param("mac") String macAddress, @Param("ip") String ipAddress, @Param("hostname") String hostname, @Param("status") Integer status);
}
