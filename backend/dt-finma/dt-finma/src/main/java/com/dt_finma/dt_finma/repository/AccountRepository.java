package com.dt_finma.dt_finma.repository;

import com.dt_finma.dt_finma.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

}

