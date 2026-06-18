package gestion.scolaire.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "parametre_ecole")
public class ParametreEcole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomEcole;
    private String adresseEcole;
    private String telephoneEcole;

    /** Part de la note de classe dans la moyenne (ex: 40.0 = 40 %) */
    private double quotaClasse = 40.0;

    /** Part de la note de composition dans la moyenne (ex: 60.0 = 60 %) */
    private double quotaComposition = 60.0;
}
