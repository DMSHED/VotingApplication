package com.votingapp.database.repository;

import com.votingapp.database.entity.Topic;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer> {

    Optional<Topic> findByNameIgnoreCase(String name);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    List<Topic> findAll();


}
