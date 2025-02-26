package com.votingapp.service;

import com.votingapp.database.entity.Topic;
import com.votingapp.database.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;

    public Topic create(Topic topic) {
        return Optional.of(topic)
                .map(topicRepository::save)
                .orElseThrow();
    }

    public Topic findByName(String name) {
        return topicRepository.findByNameContainingIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }


}
