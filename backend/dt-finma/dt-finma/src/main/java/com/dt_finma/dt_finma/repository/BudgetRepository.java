package com.dt_finma.dt_finma.repository;

import com.dt_finma.dt_finma.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByCategory_User_Id(Long userId);

}