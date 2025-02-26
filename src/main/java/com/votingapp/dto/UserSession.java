package com.votingapp.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserSession {

    private final String username;
    private final boolean isAuthenticated;

}
