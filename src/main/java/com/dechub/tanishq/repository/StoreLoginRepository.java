package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.StoreLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreLoginRepository extends JpaRepository<StoreLogin, Long> {

    Optional<StoreLogin> findByStoreCodeAndPassword(String storeCode, String password);

    Optional<StoreLogin> findByStoreCode(String storeCode);

    List<StoreLogin> findByRole(String role);
}
