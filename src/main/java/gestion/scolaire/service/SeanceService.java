package gestion.scolaire.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.scolaire.dto.SeanceDTO;
import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Seance;
import gestion.scolaire.model.StatutPresence;
import gestion.scolaire.model.StatutSeance;
import gestion.scolaire.model.Classe;
import gestion.scolaire.repository.AffectationRepository;
import gestion.scolaire.repository.AppelRepository;
import gestion.scolaire.repository.NoteRepository;
import gestion.scolaire.repository.SeanceRepository;
import gestion.scolaire.repository.ClasseRepository;

@Service
public class SeanceService {

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private AffectationRepository affectationRepository;

    @Autowired
    private AppelRepository appelRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AnneeScolaireService annnAnneeScolaireService;

    @Autowired
    private ClasseRepository classeRepository;

    // 1️⃣ Démarrer une séance
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

    // 2️⃣ Terminer une séance
    public Seance terminerSeance(Long seanceId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));

        seance.setHeureFin(LocalTime.now());
        seance.setStatut(StatutSeance.TERMINEE);
        seance.setDateModification(LocalDateTime.now());

        return seanceRepository.save(seance);
    }

    // 3️⃣ Récupérer séances par date
    public List<SeanceDTO> getSeancesParDate(LocalDate date) {
        List<Seance> seances = seanceRepository.findByDate(date);
        updateSeanceStatuses(seances);
        return seances.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Récupérer séances par enseignant et date
    public List<SeanceDTO> getSeancesParEnseignantEtDate(Long enseignantId, LocalDate date) {
        List<Seance> seances = seanceRepository.findByAffectationEnseignantIdAndDate(enseignantId, date);
        // Ne plus mettre à jour le statut automatiquement : c'est manuel
        // updateSeanceStatuses(seances);
        return seances.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<SeanceDTO> getSeancesDuJour() {
        List<Seance> seances = seanceRepository.findByDate(LocalDate.now());
        // Ne plus mettre à jour le statut automatiquement : c'est manuel
        // updateSeanceStatuses(seances);
        return seances.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private void updateSeanceStatuses(List<Seance> seances) {
    LocalTime now = LocalTime.now();
    LocalDate today = LocalDate.now();

    for (Seance seance : seances) {
      LocalDate seanceDate = seance.getDate();
      StatutSeance newStatus;

      if (seanceDate.isBefore(today)) {
        // Date passée → automatiquement terminée
        newStatus = StatutSeance.TERMINEE;
      } else if (seanceDate.isEqual(today)) {
        // Date d'aujourd'hui → vérifier l'heure
        LocalTime debut = seance.getHeureDebut();
        LocalTime fin = seance.getHeureFin();

        if (debut != null && fin != null) {
          if (now.isBefore(debut)) {
            newStatus = StatutSeance.PLANIFIEE;
          } else if (now.isAfter(fin)) {
            newStatus = StatutSeance.TERMINEE;
          } else {
            newStatus = StatutSeance.EN_COURS;
          }
        } else {
          newStatus = seance.getStatut();
        }
      } else {
        // Date future → planifiée
        newStatus = StatutSeance.PLANIFIEE;
      }

      if (seance.getStatut() != newStatus) {
        seance.setStatut(newStatus);
        seance.setDateModification(LocalDateTime.now());
        seanceRepository.save(seance);
      }
    }
  }

    private SeanceDTO convertToDTO(Seance seance) {
        SeanceDTO dto = new SeanceDTO();
        dto.setId(seance.getId());
        dto.setAffectationId(seance.getAffectation().getId());
        dto.setMatiere(seance.getMatiere());
        dto.setProfesseur(seance.getAffectation().getEnseignant().getPrenom() + " "
                + seance.getAffectation().getEnseignant().getNom());
        dto.setClasse(seance.getAffectation().getClasse().getNom());
        dto.setClasseId(seance.getAffectation().getClasse().getId());
        dto.setFiliere(seance.getAffectation().getClasse().getFiliere().getNom());
        dto.setHeureDebut(seance.getHeureDebut() != null ? seance.getHeureDebut().toString() : "");
        dto.setHeureFin(seance.getHeureFin() != null ? seance.getHeureFin().toString() : "");
        dto.setStatut(seance.getStatut().name());
        return dto;
    }

    // 4️⃣ Récupérer séances par affectation
    public List<Seance> getSeancesParAffectation(Long affectationId) {
        return seanceRepository.findByAffectationId(affectationId);
    }

    // 5️⃣ Récupérer séances par affectation et date
    public List<Seance> getSeancesParAffectationEtDate(Long affectationId, LocalDate date) {
        return seanceRepository.findByAffectationIdAndDate(affectationId, date);
    }

    // 6️⃣ Récupérer toutes les séances en cours
    public List<Seance> getSeancesEnCours() {
        return seanceRepository.findByStatut(StatutSeance.EN_COURS);
    }

    public List<Seance> getSeances() {
        return seanceRepository.findAll();
    }

    // Récupérer les séances du jour d'un enseignant spécifique
    public List<SeanceDTO> getSeancesDuJourParEnseignant(Long enseignantId) {
        List<Seance> seances = seanceRepository.findByAffectationEnseignantIdAndDate(enseignantId, LocalDate.now());
        updateSeanceStatuses(seances);
        return seances.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Map<String, Object> getAbsencesDuJourParEnseignant(Long enseignantId) {
        LocalDate aujourd_hui = LocalDate.now();
        long absences = appelRepository.countBySeanceAffectationEnseignantIdAndSeanceDateAndStatut(
                enseignantId,
                aujourd_hui,
                StatutPresence.ABSENT);

        return Map.of(
                "enseignantId", enseignantId,
                "date", aujourd_hui.toString(),
                "absences", absences
        );
    }

    public Map<String, Object> getMoyenneMatiereEnCoursParEnseignant(Long enseignantId) {
        LocalDate aujourd_hui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();

        List<Seance> seances = seanceRepository.findByAffectationEnseignantIdAndDate(enseignantId, aujourd_hui);

        var seanceEnCours = seances.stream()
                .filter(s -> s.getHeureDebut() != null && s.getHeureFin() != null
                        && !maintenant.isBefore(s.getHeureDebut())
                        && !maintenant.isAfter(s.getHeureFin()))
                .findFirst();

        if (seanceEnCours.isEmpty()) {
            seanceEnCours = seances.stream()
                    .filter(s -> s.getHeureDebut() != null && s.getHeureDebut().isAfter(maintenant))
                    .min(Comparator.comparing(Seance::getHeureDebut));
        }

        if (seanceEnCours.isEmpty()) {
            return Map.of(
                    "enseignantId", enseignantId,
                    "date", aujourd_hui.toString(),
                    "message", "Aucune séance en cours ou prochaine aujourd'hui pour cet enseignant"
            );
        }

        Seance seance = seanceEnCours.get();
        String matiere = seance.getMatiere();

        if (matiere == null || matiere.isBlank()) {
            return Map.of(
                    "enseignantId", enseignantId,
                    "date", aujourd_hui.toString(),
                    "message", "Impossible de déterminer la matière de la séance en cours ou à venir"
            );
        }

        Double moyenneGenerale = noteRepository.findAverageByAffectationEnseignantIdAndMatiereNom(enseignantId, matiere);
        List<Map<String, Object>> moyenneParClasse = noteRepository
                .findAverageByAffectationEnseignantIdAndMatiereNomGroupByClasse(enseignantId, matiere)
                .stream()
                .map(result -> {
                    Long classeId = (Long) result[0];
                    Double moyenne = (Double) result[1];
                    Classe classe = classeRepository.findById(classeId).orElse(null);
                    String classeNom = (classe != null) ? classe.getNom() : "Classe Inconnue";
                    int nombreEtudiants = (classe != null && classe.getInscriptions() != null) ? classe.getInscriptions().size() : 0;
                    
                    return Map.<String, Object>of(
                            "classeId", classeId,
                            "classeNom", classeNom,
                            "moyenne", moyenne != null ? moyenne : 0.0,
                            "nombreEtudiants", nombreEtudiants
                    );
                })
                .toList();

        return Map.of(
                "enseignantId", enseignantId,
                "date", aujourd_hui.toString(),
                "matiere", matiere,
                "moyenneGenerale", moyenneGenerale != null ? moyenneGenerale : 0.0,
                "moyenneParClasse", moyenneParClasse
        );
    }

    public Map<String, Object> getTempsAvantProchaineSeanceParEnseignant(Long enseignantId) {
        LocalDate aujourd_hui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();

        return seanceRepository.findByAffectationEnseignantIdAndDate(enseignantId, aujourd_hui).stream()
                .filter(s -> s.getHeureDebut() != null &&
                        (s.getHeureDebut().isAfter(maintenant) || s.getHeureDebut().equals(maintenant)))
                .min(Comparator.comparing(Seance::getHeureDebut))
                .map(prochaine -> {
                    Duration duree = Duration.between(maintenant, prochaine.getHeureDebut());
                    return Map.<String, Object>of(
                            "seanceId", prochaine.getId(),
                            "matiere", prochaine.getMatiere(),
                            "heureDebut", prochaine.getHeureDebut().toString(),
                            "heureFin", prochaine.getHeureFin() != null ? prochaine.getHeureFin().toString() : null,
                            "minutesRestantes", duree.toMinutes(),
                            "heuresRestantes", duree.toHours(),
                            "statut", prochaine.getStatut() != null ? prochaine.getStatut().name() : null
                    );
                })
                .orElse(Map.of("message", "Aucune séance restante aujourd'hui pour cet enseignant"));
    }
}
