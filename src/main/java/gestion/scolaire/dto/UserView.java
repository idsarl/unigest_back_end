package gestion.scolaire.dto;

import org.springframework.beans.factory.annotation.Value;
import gestion.scolaire.model.Role;

public interface UserView {

    @Value("#{target.id}")
    Long getId();

    @Value("#{target.id}")
    Long getIdUser();

    @Value("#{target.email}")
    String getUsername();

    String getTelephone();

    String getEmail();

    @Value("#{target instanceof T(gestion.scolaire.model.Parent) ? target.adresse : (target instanceof T(gestion.scolaire.model.Enseignant) ? target.adresse : null)}")
    String getAdresse();

    Role getRole();
}


