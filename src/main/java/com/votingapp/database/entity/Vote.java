package com.votingapp.database.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@ToString(exclude = "topic")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vote")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class Vote {

    //id,name,description,topic_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by")
    private String created_by;

    @JoinColumn(name = "topic_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Topic topic;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vote_results", joinColumns = @JoinColumn(name = "vote_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "count")
    private Map<String, Integer> results = new HashMap<>();

    public void setTopic(Topic topic) {
        this.topic = topic;
        this.topic.getVotes().add(this);
    }
}

