package com.votingapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.votingapp.database.entity.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveService {
    @Value("${path.save}")
    private final String savePath;
    private final TopicService topicService;
    private final VoteService voteService;
    private final ObjectMapper objectMapper;

    public void saveFile(String fileName) {
        Path dirPath = Path.of(savePath);
        try {
            //создаем директорию
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            //полный путь к файлу
            File file = new File(dirPath + File.separator+ fileName);
            List<Topic> topics = topicService.findAll();

            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(file, topics);

            // Сохраняем данные в JSON формате
            log.info("Data saved successfully to {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error save data: {}", e.getMessage());
        }
    }
}
