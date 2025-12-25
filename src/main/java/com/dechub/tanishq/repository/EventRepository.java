package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    @Query("SELECT e FROM Event e WHERE e.store.storeCode = :storeCode ORDER BY e.createdAt ASC")
    List<Event> findByStoreCode(@Param("storeCode") String storeCode);

    @Query("SELECT e FROM Event e WHERE e.store.storeCode IN :storeCodes ORDER BY e.createdAt ASC")
    List<Event> findByStoreCodeIn(@Param("storeCodes") List<String> storeCodes);
}
