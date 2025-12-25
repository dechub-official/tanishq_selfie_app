package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.RbmLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RbmLoginRepository extends JpaRepository<RbmLogin, Long> {
    Optional<RbmLogin> findByRbmUserIdAndPassword(String rbmUserId, String password);
    Optional<RbmLogin> findByRbmUserId(String rbmUserId);
}
