package com.dt_finma.dt_finma.repository;

import com.dt_finma.dt_finma.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByAccount_User_Id(Long userId);

}