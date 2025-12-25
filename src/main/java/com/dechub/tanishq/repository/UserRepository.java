package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPassword(String username, String password);
    List<User> findByRole(String role);
    Optional<User> findByUsername(String username);
}
