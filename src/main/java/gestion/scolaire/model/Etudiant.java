package gestion.scolaire.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class Etudiant extends Utilisateur {

    @Column(unique = true)
    private String matricule;

    private LocalDate dateNaissance;

    @ManyToOne
    private Parent parent;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnore
    private List<Inscription> inscription;

}