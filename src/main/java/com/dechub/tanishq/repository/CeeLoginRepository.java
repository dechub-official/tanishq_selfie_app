package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.CeeLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CeeLoginRepository extends JpaRepository<CeeLogin, Long> {
    Optional<CeeLogin> findByCeeUserIdAndPassword(String ceeUserId, String password);
    Optional<CeeLogin> findByCeeUserId(String ceeUserId);
}
