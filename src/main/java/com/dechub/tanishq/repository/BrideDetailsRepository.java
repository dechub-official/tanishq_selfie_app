package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.BrideDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrideDetailsRepository extends JpaRepository<BrideDetails, Long> {
}
