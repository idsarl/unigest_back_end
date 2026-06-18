package gestion.scolaire.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class Enseignant extends Utilisateur {

    private String specialite;
    private String adresse;

    @OneToMany(mappedBy = "enseignant")
    @JsonIgnore
    private List<Affectation> affectations;

}