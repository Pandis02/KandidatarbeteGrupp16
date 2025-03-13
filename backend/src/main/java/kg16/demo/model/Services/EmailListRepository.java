package kg16.demo.model.Services;

import kg16.demo.model.Services.EmailList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailListRepository extends JpaRepository<EmailList, String> {

    @Query("SELECT e.mailAddress FROM EmailList e")
    List<String> findAllEmails();
}
