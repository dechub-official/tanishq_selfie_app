package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, String> {
    // Delete by btqCode (store code)
    @Modifying
    @Query("DELETE FROM PasswordHistory ph WHERE ph.btqCode = :btqCode")
    void deleteByBtqCode(@Param("btqCode") String btqCode);
}
