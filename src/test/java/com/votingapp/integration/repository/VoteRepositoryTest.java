package com.votingapp.integration.repository;


import com.votingapp.database.entity.Topic;
import com.votingapp.database.entity.Vote;
import com.votingapp.database.repository.TopicRepository;
import com.votingapp.database.repository.VoteRepository;
import com.votingapp.integration.IntegrationTestBase;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class VoteRepositoryTest extends IntegrationTestBase {

    private final VoteRepository voteRepository;
    private final TopicRepository topicRepository;
    private final EntityManager em;

    @Test
    void saveVote() {
        Topic topic = Topic.builder()
                .name("topic")
                .created_by("created_by")
                .build();

        

        Vote vote = Vote.builder()
                .name("test")
                .description("test")
                .topic(topic)
                .results(Map.of(
            "Вредно",0,
                                 "Полезно",0
    ))
            .build();
        Vote vote2 = Vote.builder()
                .name("test2")
                .description("test")
                .topic(topic)
                .results(Map.of(
            "Вредно",0,
                                 "Полезно",0
    ))
            .build();

        topicRepository.save(topic);
        vote.setTopic(topic);
        vote2.setTopic(topic);
        voteRepository.save(vote);
        voteRepository.save(vote2);

        Optional<Topic> byId = topicRepository.findById(topic.getId());
        byId.ifPresent(topic1 -> {
            System.out.println(topic1.getVotes());
        });

    }
}
