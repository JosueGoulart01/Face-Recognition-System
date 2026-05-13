package com.example.facerecognition.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome da pessoa
    @Column(nullable = false)
    private String name;

    // Label usado pelo LBPH
    @Column(unique = true)
    private Integer label;
}