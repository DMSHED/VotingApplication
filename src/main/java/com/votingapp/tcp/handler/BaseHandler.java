package com.votingapp.tcp.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public abstract class BaseHandler extends ChannelDuplexHandler {
    protected boolean canHandle(String command) {
        return false; // по умолчанию обработчик не может обрабатывать команды;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;

        if (canHandle(message)) {
            handleCommand(ctx, message); // обработка команды
        } else {
            ctx.fireChannelRead(msg); // Передача команды дальше по цепочке
        }
    }

    protected abstract void handleCommand(ChannelHandlerContext ctx, String msg);
}
