package gestion.scolaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion.scolaire.model.EmploiDuTemps;

public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {

    List<EmploiDuTemps> findByClasseId(Long classeId);

    List<EmploiDuTemps> findByEnseignantId(Long enseignantId);

    List<EmploiDuTemps> findByActifTrue();
}