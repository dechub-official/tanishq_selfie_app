package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.PasswordChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordChangeHistoryRepository extends JpaRepository<PasswordChangeHistory, Long> {

    // Optional helper to see history for a store
    List<PasswordChangeHistory> findByStoreCodeOrderByChangedAtDesc(String storeCode);
}
