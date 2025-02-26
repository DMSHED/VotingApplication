package com.votingapp.tcp;

import com.votingapp.service.TopicService;
import com.votingapp.service.VoteService;
import com.votingapp.tcp.handler.ClientRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VotingServerInitializer extends ChannelInitializer<SocketChannel> {

    private final TopicService topicService;
    private final VoteService voteService;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Добавляем декодер для преобразования байтов в строки
        pipeline.addLast("decoder",new StringDecoder());

        // Добавляем кодировщик для преобразования строк в байты
        pipeline.addLast("encoder",new StringEncoder());

        pipeline.addLast("clientRequestHandler",new ClientRequestHandler(topicService, voteService));
    }
}
