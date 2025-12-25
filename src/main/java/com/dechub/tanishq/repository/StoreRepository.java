package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    List<Store> findByAbmUsername(String abmUsername);
    List<Store> findByRbmUsername(String rbmUsername);
    List<Store> findByCeeUsername(String ceeUsername);

    // Group stores by AB username, RB username, CE username for regional analysis
    @Query("SELECT s FROM Store s WHERE s.abmUsername = ?1")
    List<Store> findStoresByAbm(String abmUsername);

    @Query("SELECT s FROM Store s WHERE s.rbmUsername = ?1")
    List<Store> findStoresByRbm(String rbmUsername);

    @Query("SELECT s FROM Store s WHERE s.ceeUsername = ?1")
    List<Store> findStoresByCee(String ceeUsername);
}
