package com.votingapp.service;

import com.votingapp.database.entity.Topic;
import com.votingapp.database.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

    public Topic findByNameIgnoreCase(String name) {
        return topicRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    public List<Topic> findAll() {
        return topicRepository.findAll();
    }


    public void saveAll(List<Topic> topics) {
        for (Topic topic : topics) {
            if (topic.getId() == null) { // Если объект новый
                Optional<Topic> existingTopic = topicRepository.findByNameIgnoreCase(topic.getName());
                // Обновляем существующий объект
                existingTopic.ifPresent(value -> topic.setId(value.getId()));
            }
        }
        topicRepository.saveAll(topics);
    }
}
