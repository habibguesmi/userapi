package com.example.userapi.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   // @JsonProperty("nom")
    private String name;// nom complet

   // @JsonProperty("pseudo")
    private String username; // pseudo

   // @JsonProperty("statut")
    private String status;  // statut, par ex. "active" ou "inactive"

    private Double budget;   // budget
}