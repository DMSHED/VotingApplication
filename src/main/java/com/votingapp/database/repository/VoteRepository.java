package com.votingapp.database.repository;

import com.votingapp.database.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Integer> {


    Optional<Vote> findByNameIgnoreCase(String name);
}
