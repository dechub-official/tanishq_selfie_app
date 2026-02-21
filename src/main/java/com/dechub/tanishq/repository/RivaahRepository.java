package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.Rivaah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RivaahRepository extends JpaRepository<Rivaah, Long> {
    Optional<Rivaah> findByCode(String code);
}
