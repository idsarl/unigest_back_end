package gestion.scolaire.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class Alerte {

    @Id
    private String id;

    @Column(nullable = true)
    private String sujet;

    @Column(nullable = true)
    private String email;

    @Column(length = 2000, nullable = false)
    private String message;

    private String dateAjout;

    public Alerte() {
    }

    public Alerte(String email, String message, String sujet){
      this.email = email;
      this.message = message;
      this.sujet = sujet;
    }

  public Alerte(String email, String message){

    this.email = email;
    this.message = message;

  }
}
