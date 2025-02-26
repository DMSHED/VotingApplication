package com.votingapp.dto;

import com.votingapp.database.entity.Topic;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VoteCreateState {
    private Topic topic; // Текущий раздел
    private String voteName; // Название голосования
    private String voteDescription; // Описание голосования
    private Integer optionsCount; // Количество вариантов ответа
    private List<String> options = new ArrayList<>(); // Варианты ответа
    private String created_by;
    private State currentState = State.WAITING_FOR_NAME;

    public VoteCreateState(Topic topic, String created_by) {
        this.topic = topic;
        this.created_by = created_by;
    }

    public enum State {
        WAITING_FOR_NAME,
        WAITING_FOR_DESCRIPTION,
        WAITING_FOR_OPTIONS_COUNT,
        WAITING_FOR_OPTIONS,
        COMPLETED
    }

}
