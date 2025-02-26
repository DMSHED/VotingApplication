package com.votingapp.database.repository;

import com.votingapp.database.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer> {

    Optional<Topic> findByNameContainingIgnoreCase(String name);
}
