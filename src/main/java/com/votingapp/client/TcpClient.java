package com.votingapp.client;

import com.votingapp.client.handler.TcpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Component
@RequiredArgsConstructor
public class TcpClient {
    @Value("${client.host}")
    private final String host;
    @Value("${client.port}")
    private final Integer port;

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)  // Используем NIO
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());  // Декодируем байты в строки
                            pipeline.addLast(new StringEncoder());  // Кодируем строки в байты
                            pipeline.addLast(new TcpClientHandler());  // Обработчик входящих сообщений
                        }
                    });

            // Подключение к серверу
            ChannelFuture future = bootstrap.connect(host, port).sync();
            System.out.println("Подключено к серверу " + host + ":" + port);

            // Отправка сообщения
            Channel channel = future.channel();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (!Thread.interrupted()) {
                    String message = reader.readLine().trim();
                    if ("exit".equals(message)) {
                        channel.writeAndFlush("exit\n");
                        break;
                    }
                    channel.writeAndFlush(message + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Ожидание завершения работы клиента
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
