package com.votingapp.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString(exclude = "votes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "topic")
public class Topic {
    // id, name, created_by
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_by")
    private String created_by;

    @Builder.Default
    @OneToMany(mappedBy = "topic", fetch = FetchType.EAGER)
    private List<Vote> votes = new ArrayList<>();

}
