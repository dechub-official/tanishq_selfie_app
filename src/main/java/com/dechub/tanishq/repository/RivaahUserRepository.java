package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.RivaahUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RivaahUserRepository extends JpaRepository<RivaahUser, Long> {
}
