package com.votingapp.mapper;

import com.votingapp.database.entity.Vote;
import com.votingapp.dto.VoteState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class VoteCreateMapper implements Mapper<VoteState, Vote>{
    @Override
    public Vote map(VoteState object) {

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
