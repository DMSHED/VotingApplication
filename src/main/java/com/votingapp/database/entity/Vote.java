package com.votingapp.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vote")
public class Vote {

    //id,name,description,topic_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @JoinColumn(name = "topic_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Topic topic;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "vote_results", joinColumns = @JoinColumn(name = "vote_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "count")
    private Map<String, Integer> results = new HashMap<>();

    public void setTopic(Topic topic) {
        this.topic = topic;
        this.topic.getVotes().add(this);
    }
}

