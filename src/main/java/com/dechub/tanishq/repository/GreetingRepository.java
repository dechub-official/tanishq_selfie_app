package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.Greeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {
    Optional<Greeting> findByUniqueId(String uniqueId);
}
