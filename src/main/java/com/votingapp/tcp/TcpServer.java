package com.votingapp.tcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.votingapp.database.entity.Topic;
import com.votingapp.service.TopicService;
import com.votingapp.service.VoteService;
import com.votingapp.tcp.handler.ClientRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class TcpServer {

    @Value("${tcpserver.port}")
    private final int port;
    @Value("${path.load}")
    private final String loadPath;
    @Value("${path.save}")
    private final String savePath;

    private final TopicService topicService;
    private final VoteService voteService;
    private final ObjectMapper objectMapper;

    public void run(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();  // Для обработки входящих подключений
        EventLoopGroup workerGroup = new NioEventLoopGroup();  // Для обработки данных

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)  // Используем NIO
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());  // Декодируем байты в строки
                            pipeline.addLast(new StringEncoder());  // Кодируем строки в байты
                            pipeline.addLast(new ClientRequestHandler(topicService, voteService));                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)  // Максимальное количество ожидающих подключений
                    .childOption(ChannelOption.SO_KEEPALIVE, true);  // Поддержка keep-alive

            // Запуск сервера
            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("TCP сервер запущен на порту {}", port);

            //получение сообщения на сам же сервере
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (!Thread.interrupted()) {
                    String message = reader.readLine().trim();
                    if ("exit".equals(message)) {
                        log.info("Server stopped");
                        break;
                    }
                    String[] parts = message.split(" ");

                    if (parts.length < 2) {
                        log.info("Invalid command");
                        continue;
                    }

                    String command = parts[0].trim();
                    switch (command){
                        case "load":
                            loadFile(parts[1].trim());
                            break;
                        case "save":
                            saveFile(parts[1].trim());
                            break;
                        default:
                            log.error("Unknown command {}", command);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Ожидание завершения работы сервера
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void saveFile(String fileName) {
        Path dirPath = Path.of(loadPath);
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

    private void loadFile(String fileName) {
        Path dirPath = Path.of(savePath);
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
