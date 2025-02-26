package com.votingapp.tcp.handler;

import com.votingapp.database.entity.Topic;
import com.votingapp.database.entity.Vote;
import com.votingapp.dto.UserSession;
import com.votingapp.dto.VoteCreateState;
import com.votingapp.service.TopicService;
import com.votingapp.service.VoteService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultPromise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ClientRequestHandler extends BaseHandler{

    //будет кешем для сессий юзеров
    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();
    // кеш для состояния создания голосования конкретного юзера
    private final Map<String, VoteCreateState> voteCreateStates = new ConcurrentHashMap<>();

    private final TopicService topicService;
    private final VoteService voteService;

    @Override
    protected boolean canHandle(String command) {
        return true;
    }

    @Override
    protected void handleCommand(ChannelHandlerContext ctx, String message) {

        String[] parts = message.split("-");
        String command = parts[0].trim();

        //проверяем и логиним пользователя
        if (!isUserAuthenticated(ctx) && !command.equals("exit")) {
            handleLogin(ctx, message);
            return;
        }

        //запрашиваем из кеш, существует ли процесс/состояние для создания голосования
        VoteCreateState state = voteCreateStates.get(ctx.name());

        if (state != null) {
            processVoteCreationInput(ctx, message, state);
            return;
        }

        switch (command) {
            case "create topic":
                handleCreateTopic(ctx, parts);
                break;
            case "view":
                handleView(ctx, parts);
                break;
            case "create vote":
                handleCreateVote(ctx, parts);
                break;
            case "exit":
                handleExit(ctx);
                break;


            default:
                ctx.writeAndFlush("Unknown command\n");
        }
    }
    //<topic (votes in topic=<count>)>
    private void handleView(ChannelHandlerContext ctx, String[] parts) {
        if (parts.length == 2 && parts[1].startsWith("t=")) {
            try {
                String topicName = parts[1].substring(2).trim();
                Topic topic = topicService.findByName(topicName);

                StringBuffer stringBuffer = new StringBuffer();

                stringBuffer.append("Votes in  topic "+ topic.getName() + ": \n {\n");
                topic.getVotes().forEach(vote -> {
                    stringBuffer.append("  " + vote.getName() + "\n");
                });
                stringBuffer.append(" }");


                ctx.writeAndFlush(stringBuffer);
            } catch (ResponseStatusException e) {
                ctx.writeAndFlush("Topic name not found\n");
            }
        } else if (parts.length == 3 && parts[1].startsWith("t=") && parts[2].startsWith("v="))  {
            try {
                String topicName = parts[1].substring(2).trim();
                String voteName = parts[2].substring(2).trim();

                //нужен лишь для того, чтобы проверить, что запрошенный topic существует
                //если не существует выбросит ошибку ResponseStatusException
                Topic topic = topicService.findByName(topicName);
                //получаем Vote, также, если не нашли, пробросит исключение
                Vote vote = voteService.findByNameIgnoreCase(voteName);

                StringBuffer stringBuffer = new StringBuffer();

                stringBuffer.append("topic " + topicName +":\n " + "vote "+voteName + ":\n  {\n");

                vote.getResults().forEach((key, value) -> {
                    stringBuffer.append("   " + key + ": " + value + "\n");
                });

                stringBuffer.append("  }\n");

                ctx.writeAndFlush(stringBuffer);
            } catch (ResponseStatusException e) {
                ctx.writeAndFlush("topic or vote not found\n");
            }
        } else {
            List<Topic> topics = topicService.findAll();

            StringBuffer stringBuffer = new StringBuffer();

            topics.forEach(topic -> {
                stringBuffer.append(topic.getName()).append("(votes in topic=").append(topic.getVotes().size()).append(")\n");
            });
            ctx.writeAndFlush(stringBuffer.toString());
        }

    }

    private void processVoteCreationInput(ChannelHandlerContext ctx, String input, VoteCreateState state) {
        switch (state.getCurrentState()) {
            case WAITING_FOR_NAME:
                if (Objects.equals(input, "cancel")) {
                    failedVoteCreation(ctx,state);
                    return;
                }
                if (isExistNameVote(input)) {
                    ctx.writeAndFlush("This name already used: \n");
                    return;
                }

                state.setVoteName(input);
                state.setCurrentState(VoteCreateState.State.WAITING_FOR_DESCRIPTION);
                ctx.writeAndFlush("Enter vote description or enter cancel: \n");
                break;
            case WAITING_FOR_DESCRIPTION:
                if (Objects.equals(input, "cancel")) {
                    failedVoteCreation(ctx,state);
                    return;
                }
                state.setVoteDescription(input);
                state.setCurrentState(VoteCreateState.State.WAITING_FOR_OPTIONS_COUNT);
                ctx.writeAndFlush("Enter number of options or enter cancel: \n");
                break;

            case WAITING_FOR_OPTIONS_COUNT:
                if (Objects.equals(input.trim(), "cancel")) {
                    failedVoteCreation(ctx,state);
                    return;
                }
                try {
                    int optionsCount = Integer.parseInt(input);
                    // Исключение , если не число или если меньше 0
                    if ( optionsCount <= 0 || !input.matches("-?\\d+")) {
                        throw new NumberFormatException();
                    }
                    state.setOptionsCount(optionsCount);
                    ctx.writeAndFlush("Enter option 1 or enter cancel: \n");

                } catch (NumberFormatException e) {
                    ctx.writeAndFlush("Invalid number. Enter number of options or enter cancel: \n");
                    break;
                }
                state.setCurrentState(VoteCreateState.State.WAITING_FOR_OPTIONS);

                break;
            case WAITING_FOR_OPTIONS:
                if (Objects.equals(input, "cancel")) {
                    failedVoteCreation(ctx,state);
                    return;
                }
                state.getOptions().add(input);

                if (state.getOptions().size() < state.getOptionsCount()) {
                    ctx.writeAndFlush("Enter option or enter cancel " + (state.getOptions().size() + 1) + ": \n");
                } else {
                    completeVoteCreation(ctx, state);
                }
                break;
        }
    }

    private boolean isExistNameVote(String input) {
        try {
            voteService.findByNameIgnoreCase(input);
            return true;
        } catch (ResponseStatusException e) {
            return false;
        }
    }

    private void failedVoteCreation(ChannelHandlerContext ctx, VoteCreateState state) {
        voteCreateStates.remove(ctx.name()); // Очищаем состояние после завершения
        ctx.writeAndFlush("Vote created stopped \n");
    }

    private void completeVoteCreation(ChannelHandlerContext ctx, VoteCreateState state) {
        voteService.create(state);
        voteCreateStates.remove(ctx.name()); // Очищаем состояние после завершения
        ctx.writeAndFlush("Vote created successfully in topic '" + state.getTopic().getName() + "'\n");
    }


    private void handleCreateVote(ChannelHandlerContext ctx, String[] parts) {
        if (parts.length == 2 && parts[1].startsWith("t=")) {
            String topicName = parts[1].substring(2).trim();

            try {
                Topic topic = topicService.findByName(topicName);
                System.out.println(topic);

                //создаем обьект состояния создания голосования
                VoteCreateState state = new VoteCreateState(topic, userSessions.get(ctx.name()).getUsername());
                //помещаем в кэш так сказать, чтобы отслеживать
                voteCreateStates.put(ctx.name(), state);

                ctx.writeAndFlush("Enter vote name (unique) or enter cancel: \n");
            } catch (ResponseStatusException ex) {
                ctx.writeAndFlush("Invalid topic name: " + topicName + "\n");
            }
        } else {
            ctx.writeAndFlush("Invalid create vote command\n");
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
                        .orElseThrow(RuntimeException::new);
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
