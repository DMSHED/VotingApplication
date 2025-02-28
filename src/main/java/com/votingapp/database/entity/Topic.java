package com.votingapp.database.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "topic")
public class Topic {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_by")
    private String created_by;

    @JsonManagedReference // Для родительской стороны
    @Builder.Default
    @OneToMany(mappedBy = "topic", fetch = FetchType.EAGER)
    private List<Vote> votes = new ArrayList<>();

}
