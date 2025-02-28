package com.votingapp.database.repository;

import com.votingapp.database.entity.Vote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Integer> {


    Optional<Vote> findByNameIgnoreCase(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Vote saveAndFlush(Vote vote);


}
