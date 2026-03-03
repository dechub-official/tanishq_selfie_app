package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.CorporateLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporateLoginRepository extends JpaRepository<CorporateLogin, Long> {
    Optional<CorporateLogin> findByCorporateUserIdAndPassword(String corporateUserId, String password);
    Optional<CorporateLogin> findByCorporateUserId(String corporateUserId);
}

