package com.votingapp.mapper;

import com.votingapp.database.entity.Topic;
import com.votingapp.database.entity.Vote;
import com.votingapp.database.repository.TopicRepository;
import com.votingapp.dto.VoteCreateState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class VoteCreateMapper implements Mapper<VoteCreateState, Vote>{
    @Override
    public Vote map(VoteCreateState object) {

        Map<String, Integer> res = new HashMap<>();
        for (String item : object.getOptions()) {
            res.put(item, 0);
        }

        return Vote.builder()
                .name(object.getVoteName())
                .description(object.getVoteDescription())
                .topic(object.getTopic())
                .results(res)
                .created_by(object.getCreated_by())
                .build();
    }
}
