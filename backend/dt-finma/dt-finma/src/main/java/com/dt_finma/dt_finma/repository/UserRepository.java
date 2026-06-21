package com.dt_finma.dt_finma.repository;

import com.dt_finma.dt_finma.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);


}
