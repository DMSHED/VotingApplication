package com.votingapp.tcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.votingapp.database.entity.Topic;
import com.votingapp.service.LoadService;
import com.votingapp.service.SaveService;
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

    private final TopicService topicService;
    private final VoteService voteService;
    private final LoadService loadService;
    private final SaveService saveService;

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
                            loadService.loadFile(parts[1].trim());
                            break;
                        case "save":
                            saveService.saveFile(parts[1].trim());
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

}
