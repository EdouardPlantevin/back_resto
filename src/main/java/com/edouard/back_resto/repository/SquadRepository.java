package com.edouard.back_resto.repository;

import com.edouard.back_resto.entity.Squad;
import com.edouard.back_resto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SquadRepository extends JpaRepository<Squad, Long> {
    Optional<Squad> findByCodeJoin(String codeJoin);

    Optional<Squad> findByIdAndUsers(Long id, Set<User> users);
}
