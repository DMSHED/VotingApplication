package com.votingapp.service;

import com.votingapp.database.entity.Topic;
import com.votingapp.database.entity.Vote;
import com.votingapp.database.repository.TopicRepository;
import com.votingapp.database.repository.VoteRepository;
import com.votingapp.dto.VoteCreateState;
import com.votingapp.mapper.VoteCreateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteCreateMapper voteCreateMapper;

    public Vote create(VoteCreateState vote) {
        return Optional.of(vote)
                .map(voteCreateMapper::map)
                .map(voteRepository::save)
                .orElseThrow();
    }

}
