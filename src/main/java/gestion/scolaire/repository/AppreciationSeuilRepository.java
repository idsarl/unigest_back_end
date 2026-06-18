package gestion.scolaire.repository;

import gestion.scolaire.model.AppreciationSeuil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppreciationSeuilRepository extends JpaRepository<AppreciationSeuil, Long> {

    /** Retourne les seuils triés du plus élevé au plus bas */
    List<AppreciationSeuil> findAllByOrderBySeuilMinDesc();
}
