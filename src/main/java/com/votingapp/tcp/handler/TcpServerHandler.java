package com.votingapp.tcp.handler;

import com.votingapp.database.entity.Topic;
import com.votingapp.database.repository.TopicRepository;
import com.votingapp.service.TopicService;
import com.votingapp.tcp.session.UserSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Обработчик входящих сообщений
@RequiredArgsConstructor
public class TcpServerHandler extends ChannelInboundHandlerAdapter {

    //будет кешем для сессий юзеров
    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();

    private final TopicService topicService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = (String) msg;

        String[] parts = message.split("-");
        String command = parts[0].trim();

        //проверяем и логиним пользователя
        if (!isUserAuthenticated(ctx) && !command.equals("exit")) {
            handleLogin(ctx, message);
            return;
        }

        switch (command) {
            case "create topic":
                handleCreateTopic(ctx, parts);
                break;
//            case "view":
//                handleView(parts, ctx);
//                break;
//            case "create vote":
//                handleCreateVote(parts);
//                break;
            case "exit":
                handleExit(ctx);
                break;
            default:
                ctx.writeAndFlush("Unknown command\n");
        }
    }

    //обработчик выхода из сессии юзера
    private void handleExit(ChannelHandlerContext ctx) {
        userSessions.remove(ctx.name());
        ctx.writeAndFlush("Your session has been closed\n");
        ctx.close();
    }

    //обработчик создания раздела
    private void handleCreateTopic(ChannelHandlerContext ctx, String[] parts) {
        if (parts.length == 2 && parts[1].startsWith("n=")) {
            String topicName = parts[1].substring(2).trim();

            Topic topic = Topic.builder()
                    .name(topicName)
                    .created_by(userSessions.get(ctx.name()).getUsername())
                    .build();
            try {
                Optional.of(topicService.create(topic))
                        .map(it -> ctx.writeAndFlush("Topic id: " +it.getId() + ", name topic: "+ it.getName() + " has been created\n"))
                        .orElseThrow(() -> new RuntimeException("Topic creation failed"));
            } catch (RuntimeException e) {
                ctx.writeAndFlush("Topic creation failed\n");
            }
        } else {
            ctx.writeAndFlush("Usage: create topic -n=<template name>\n");
        }
    }

    //проверяем существует ли сессия с пользователем в хеш
    private boolean isUserAuthenticated(ChannelHandlerContext ctx) {
        UserSession session = userSessions.get(ctx.name());
        return session != null && session.isAuthenticated();
    }

    //обработчик login
    private void handleLogin(ChannelHandlerContext ctx, String msg) {
        String[] parts = msg.split("-");
        String command = parts[0].trim();

        if ("login".equals(command) && parts.length == 2 && parts[1].startsWith("u=")) {
            String username = parts[1].substring(2);

            userSessions.put(ctx.name(), new UserSession(username, true));
            ctx.writeAndFlush("Logged in as " + username + "\n");

        } else {
            ctx.writeAndFlush("You need log in, usage: login -u=<username>\n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

