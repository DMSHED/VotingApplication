package com.votingapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.votingapp.database.entity.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadService {

    @Value("${path.load}")
    private final String loadPath;

    private final TopicService topicService;
    private final VoteService voteService;
    private final ObjectMapper objectMapper;

    public void loadFile(String fileName) {
        Path dirPath = Path.of(loadPath);
        try {
            // Создаем путь к файлу
            File file = new File(dirPath + File.separator + fileName);

            // Проверяем существование файла
            if (!file.exists()) {
                log.error("File not found: {}", file.getAbsolutePath());
                return;
            }

            // Десериализуем данные из файла
            List<Topic> loadedTopics = objectMapper.readValue(file, new TypeReference<List<Topic>>() {});

            topicService.saveAll(loadedTopics);
            loadedTopics.forEach(topic ->
                    voteService.saveAll(topic.getVotes()));

            log.info("Data loaded successfully from {}", file.getAbsolutePath());

        } catch (IOException e) {
            log.error("Error loading data: {}", e.getMessage());
        }
    }
}
