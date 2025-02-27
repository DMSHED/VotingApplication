package com.votingapp.service;

import com.votingapp.database.entity.Vote;
import com.votingapp.database.repository.VoteRepository;
import com.votingapp.dto.VoteState;
import com.votingapp.mapper.VoteCreateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteCreateMapper voteCreateMapper;

    public Vote create(VoteState vote) {
        return Optional.of(vote)
                .map(voteCreateMapper::map)
                .map(voteRepository::save)
                .orElseThrow();
    }

    public Vote findByNameIgnoreCase(String name) {
        return voteRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public boolean delete(Vote vote) {
        try {
            voteRepository.delete(vote);
            voteRepository.flush();
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public void update(Vote vote) {
        voteRepository.saveAndFlush(vote);
    }
}
