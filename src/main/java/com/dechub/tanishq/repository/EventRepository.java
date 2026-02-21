package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    // Native query to avoid INNER JOIN with stores table
    // This ensures orphaned events (events without matching stores) are still included
    // Filter by is_visible column (TRUE or NULL for backward compatibility)
    @Query(value = "SELECT * FROM events WHERE store_code = :storeCode AND (is_visible IS NULL OR is_visible = TRUE) ORDER BY created_at ASC",
           nativeQuery = true)
    List<Event> findByStoreCode(@Param("storeCode") String storeCode);

    // Native query to avoid INNER JOIN with stores table
    // This ensures orphaned events (events without matching stores) are still included
    // Filter by is_visible column (TRUE or NULL for backward compatibility)
    @Query(value = "SELECT * FROM events WHERE store_code IN :storeCodes AND (is_visible IS NULL OR is_visible = TRUE) ORDER BY created_at ASC",
           nativeQuery = true)
    List<Event> findByStoreCodeIn(@Param("storeCodes") List<String> storeCodes);
}
