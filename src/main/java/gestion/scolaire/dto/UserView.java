package gestion.scolaire.dto;

import gestion.scolaire.model.Role;

import org.springframework.beans.factory.annotation.Value;

public interface UserView {

    @Value("#{target.id}")
    Long getIdUser();

    @Value("#{target.id}")
    Long getId();

    @Value("#{target.email}")
    String getUsername();

    String getTelephone();

    String getEmail();

    @Value("#{target.class.simpleName == 'Enseignant' ? target.adresse : (target.class.simpleName == 'Parent' ? target.adresse : null)}")
    String getAdresse();

    @Value("#{null}")
    String getDateEnregistrement();

    Role getRole();
}


