package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.AbmLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbmLoginRepository extends JpaRepository<AbmLogin, Long> {
    Optional<AbmLogin> findByAbmUserIdAndPassword(String abmUserId, String password);
    Optional<AbmLogin> findByAbmUserId(String abmUserId);
}
