package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, String> {
    // No need for findByBtqCode methods since btqCode is now the primary key
    // Can use findById(btqCode) directly
}
