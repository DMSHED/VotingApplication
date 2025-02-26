package com.votingapp.database.repository;

import com.votingapp.database.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer> {

    Optional<Topic> findByNameIgnoreCase(String name);

    List<Topic> findAll();


}
