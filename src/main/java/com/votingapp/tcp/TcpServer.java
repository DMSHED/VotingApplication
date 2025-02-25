package com.votingapp.tcp;

import com.votingapp.service.TopicService;
import com.votingapp.tcp.handler.TcpServerHandler;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class TcpServer {

    @Value("${tcpserver.port}")
    private final int port;

    private final TopicService topicService;

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
                            pipeline.addLast(new TcpServerHandler(topicService));  // Обработчик входящих сообщений
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)  // Максимальное количество ожидающих подключений
                    .childOption(ChannelOption.SO_KEEPALIVE, true);  // Поддержка keep-alive

            // Запуск сервера
            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("TCP сервер запущен на порту {}", port);

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
