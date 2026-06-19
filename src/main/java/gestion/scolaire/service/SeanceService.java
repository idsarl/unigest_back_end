package gestion.scolaire.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import gestion.scolaire.dto.SeanceDTO;
import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Seance;
import gestion.scolaire.model.StatutSeance;
import gestion.scolaire.repository.AffectationRepository;
import gestion.scolaire.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final AffectationRepository affectationRepository;
    private final AnneeScolaireService annnAnneeScolaireService;

    // ─────────────────────────────────────────────────────────────────────────
    // Écriture
    // ─────────────────────────────────────────────────────────────────────────

    public Seance demarrerSeance(Long affectationId, String matiere) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable"));
        AnneeScolaire anneeActive = annnAnneeScolaireService.getAnneeActive();

        Seance seance = new Seance();
        seance.setAffectation(affectation);
        seance.setAnneeScolaire(anneeActive);
        seance.setMatiere(matiere);
        seance.setDate(LocalDate.now());
        seance.setHeureDebut(LocalTime.now());
        seance.setStatut(StatutSeance.EN_COURS);
        seance.setDateCreation(LocalDateTime.now());
        return seanceRepository.save(seance);
    }

    public Seance terminerSeance(Long seanceId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        seance.setHeureFin(LocalTime.now());
        seance.setStatut(StatutSeance.TERMINEE);
        seance.setDateModification(LocalDateTime.now());
        return seanceRepository.save(seance);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture — toutes les méthodes filtrent par année scolaire active
    // ─────────────────────────────────────────────────────────────────────────

    /** Toutes les séances de l'année active */
    public List<Seance> getSeances() {
        Long anneeId = anneeActiveId();
        return seanceRepository.findByAnneeScolaireId(anneeId);
    }

    /** Séances d'un jour donné, année active */
    public List<Seance> getSeancesParDate(LocalDate date) {
        Long anneeId = anneeActiveId();
        return seanceRepository.findByDateAndAnneeScolaireId(date, anneeId);
    }

    /** Séances du jour courant en DTO, année active */
    public List<SeanceDTO> getSeancesDuJour() {
        Long anneeId = anneeActiveId();
        return seanceRepository.findByDateAndAnneeScolaireId(LocalDate.now(), anneeId)
                .stream()
                .map(s -> {
                    SeanceDTO dto = new SeanceDTO();
                    dto.setId(s.getId());
                    dto.setMatiere(s.getMatiere());
                    dto.setProfesseur(s.getAffectation().getEnseignant().getPrenom()
                            + " " + s.getAffectation().getEnseignant().getNom());
                    dto.setClasse(s.getAffectation().getClasse().getNom());
                    dto.setFiliere(s.getAffectation().getClasse().getFiliere().getNom());
                    dto.setHeureDebut(String.valueOf(s.getHeureDebut()));
                    dto.setHeureFin(String.valueOf(s.getHeureFin()));
                    dto.setStatut(s.getStatut().name());
                    return dto;
                })
                .toList();
    }

    /** Séances d'une affectation, année active */
    public List<Seance> getSeancesParAffectation(Long affectationId) {
        Long anneeId = anneeActiveId();
        return seanceRepository.findByAffectationIdAndAnneeScolaireId(affectationId, anneeId);
    }

    /** Séances d'une affectation à une date donnée, année active */
    public List<Seance> getSeancesParAffectationEtDate(Long affectationId, LocalDate date) {
        Long anneeId = anneeActiveId();
        return seanceRepository.findByAffectationIdAndDateAndAnneeScolaireId(affectationId, date, anneeId);
    }

    /** Séances en cours (statut EN_COURS), année active */
    public List<Seance> getSeancesEnCours() {
        Long anneeId = anneeActiveId();
        return seanceRepository.findByStatutAndAnneeScolaireId(StatutSeance.EN_COURS, anneeId);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Long anneeActiveId() {
        return annnAnneeScolaireService.getAnneeActive().getId();
    }
}
