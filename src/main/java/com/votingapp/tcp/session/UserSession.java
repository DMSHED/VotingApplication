package com.votingapp.tcp.session;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserSession {

    private final String username;
    private final boolean isAuthenticated;

}
