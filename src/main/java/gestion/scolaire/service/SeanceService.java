package gestion.scolaire.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import gestion.scolaire.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.dto.SeanceDTO;
import gestion.scolaire.repository.AffectationRepository;
import gestion.scolaire.repository.AppelRepository;
import gestion.scolaire.repository.NoteRepository;
import gestion.scolaire.repository.SeanceRepository;
import gestion.scolaire.repository.ClasseRepository;

@Service
@Transactional
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

    @Autowired
    private EmploiDuTempsService emploiDuTempsService;

    // 1️⃣ Démarrer une séance
    public SeanceDTO demarrerSeance(Long affectationId, String matiere) {

        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable"));

        // Chercher une séance planifiée pour aujourd'hui avec cette affectation et cette matière
        List<Seance> seances = seanceRepository.findByAffectationIdAndDate(affectationId, LocalDate.now());
        Seance seance = null;
        for (Seance s : seances) {
            if (s.getMatiere().equals(matiere) && s.getStatut() == StatutSeance.PLANIFIEE) {
                seance = s;
                break;
            }
        }

        if (seance == null) {
            AnneeScolaire anneeActive = annnAnneeScolaireService.getAnneeActive();
            seance = new Seance();
            seance.setAffectation(affectation);
            seance.setAnneeScolaire(anneeActive);
            seance.setMatiere(matiere);
            seance.setDate(LocalDate.now());
            seance.setHeureDebut(LocalTime.now());
            seance.setDateCreation(LocalDateTime.now());
        }

        seance.setStatut(StatutSeance.EN_COURS);
        seance.setDateModification(LocalDateTime.now());

        return convertToDTO(seanceRepository.save(seance));
    }

    // 2️⃣ Terminer une séance
    public SeanceDTO terminerSeance(Long seanceId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));

        seance.setHeureFin(LocalTime.now());
        seance.setStatut(StatutSeance.TERMINEE);
        seance.setDateModification(LocalDateTime.now());

        return convertToDTO(seanceRepository.save(seance));
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

    // 6️⃣ Récupérer toutes les séances en cours de l'année active
    public List<Seance> getSeancesEnCours() {
        return seanceRepository.findByStatutAndAnneeScolaireId(StatutSeance.EN_COURS, anneeActiveId());
    }

    public List<Seance> getSeances() {
        return seanceRepository.findByAnneeScolaireId(anneeActiveId());
    }

    private Long anneeActiveId() {
        return annnAnneeScolaireService.getAnneeActive().getId();
    }

    // Récupérer les séances du jour d'un enseignant spécifique
    public List<SeanceDTO> getSeancesDuJourParEnseignant(Long enseignantId) {
        // D'abord, générer toutes les séances manquantes pour cet enseignant aujourd'hui
        genererSeancesManquantesPourEnseignant(enseignantId);
        
        List<Seance> seances = seanceRepository.findByAffectationEnseignantIdAndDate(enseignantId, LocalDate.now());
        System.out.println("Séances après nettoyage et ajout: " + seances.size());
        updateSeanceStatuses(seances);
        return seances.stream()
                .sorted(Comparator.comparing(Seance::getHeureDebut, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::convertToDTO)
                .toList();
    }
    
    // Générer les séances manquantes pour un enseignant spécifique aujourd'hui
    private void genererSeancesManquantesPourEnseignant(Long enseignantId) {
        LocalDate aujourdHui = LocalDate.now();
        System.out.println("=== Génération séances pour enseignant " + enseignantId + " le " + aujourdHui);
        
        // 1. Récupérer tous les EmploiDuTemps du jour
        List<EmploiDuTemps> emploisDuJour = emploiDuTempsService.getByEnseignantAndDate(enseignantId, aujourdHui)
                .stream()
                .filter(e -> e.getType() == gestion.scolaire.dto.TypeEmploi.COURS)
                .toList();
        
        System.out.println("Emplois du temps trouvés: " + emploisDuJour.size());
        for (EmploiDuTemps edt : emploisDuJour) {
            System.out.println("- " + edt.getMatiere().getNom() + " " + edt.getHeureDebut() + "-" + edt.getHeureFin() + " " + edt.getClasse().getNom());
        }

        // 2. Récupérer toutes les Seances existantes pour cet enseignant aujourd'hui
        List<Seance> seancesExistantes = seanceRepository.findByAffectationEnseignantIdAndDate(enseignantId, aujourdHui);
        System.out.println("Séances existantes avant nettoyage: " + seancesExistantes.size());
        for (Seance s : seancesExistantes) {
            System.out.println("- " + s.getMatiere() + " " + s.getHeureDebut() + "-" + s.getHeureFin() + " " + s.getStatut());
        }

        // 3. Supprimer les Seances qui ne correspondent à aucun EmploiDuTemps
        List<Seance> seancesASupprimer = seancesExistantes.stream()
                .filter(seance -> {
                    // Vérifier si cette séance correspond à un EmploiDuTemps
                    return emploisDuJour.stream().noneMatch(edt -> {
                        // Vérifier si la séance correspond à l'EDT
                        if (seance.getHeureDebut() == null || seance.getHeureFin() == null) {
                            return false;
                        }
                        if (!seance.getMatiere().equals(edt.getMatiere().getNom())) {
                            return false;
                        }
                        if (!seance.getHeureDebut().equals(edt.getHeureDebut())) {
                            return false;
                        }
                        if (!seance.getHeureFin().equals(edt.getHeureFin())) {
                            return false;
                        }
                        // Vérifier la classe
                        if (seance.getAffectation() == null || seance.getAffectation().getClasse() == null) {
                            return false;
                        }
                        if (!seance.getAffectation().getClasse().getId().equals(edt.getClasse().getId())) {
                            return false;
                        }
                        return true;
                    });
                })
                .toList();
        
        System.out.println("Séances à supprimer: " + seancesASupprimer.size());
        for (Seance s : seancesASupprimer) {
            System.out.println("- Suppression: " + s.getMatiere() + " " + s.getHeureDebut() + "-" + s.getHeureFin());
            seanceRepository.delete(s);
        }

        // 4. Pour chaque EmploiDuTemps, vérifier si une Seance correspondante existe
        for (EmploiDuTemps emploi : emploisDuJour) {
            if (emploi.getClasse() == null || emploi.getEnseignant() == null || emploi.getMatiere() == null) {
                continue;
            }

            // Trouver l'affectation correspondante
            Optional<Affectation> affectationOpt = affectationRepository.findByClasseAndEnseignantAndMatiere(
                    emploi.getClasse().getId(),
                    emploi.getEnseignant().getId(),
                    emploi.getMatiere().getNom()
            );

            if (affectationOpt.isEmpty()) {
                System.out.println("Pas d'affectation pour " + emploi.getClasse().getNom() + " " + emploi.getMatiere().getNom());
                continue;
            }

            Affectation affectation = affectationOpt.get();

            // Vérifier si la séance existe déjà (après suppression)
            List<Seance> existantesPourAffectation = seanceRepository.findByAffectationIdAndDate(affectation.getId(), aujourdHui);
            boolean seanceExiste = existantesPourAffectation.stream()
                    .anyMatch(s -> s.getMatiere().equals(emploi.getMatiere().getNom()) && 
                                   s.getHeureDebut() != null &&
                                   s.getHeureDebut().equals(emploi.getHeureDebut()) &&
                                   s.getHeureFin() != null &&
                                   s.getHeureFin().equals(emploi.getHeureFin()));

            if (!seanceExiste) {
                System.out.println("Création de la séance pour " + emploi.getMatiere().getNom());
                AnneeScolaire anneeActive = annnAnneeScolaireService.getAnneeActive();
                Seance seance = new Seance();
                seance.setAffectation(affectation);
                seance.setAnneeScolaire(anneeActive);
                seance.setMatiere(emploi.getMatiere().getNom());
                seance.setDate(aujourdHui);
                seance.setHeureDebut(emploi.getHeureDebut());
                seance.setHeureFin(emploi.getHeureFin());
                seance.setStatut(StatutSeance.PLANIFIEE);
                seance.setDateCreation(LocalDateTime.now());
                seance.setDateModification(LocalDateTime.now());

                seanceRepository.save(seance);
            }
        }
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

        System.out.println("=== DEBUG getMoyenneMatiereEnCoursParEnseignant ===");
        System.out.println("enseignantId: " + enseignantId);
        System.out.println("aujourd_hui: " + aujourd_hui);
        System.out.println("maintenant: " + maintenant);

        List<EmploiDuTemps> emplois = emploiDuTempsService.getByEnseignantAndDate(enseignantId, aujourd_hui);
        System.out.println("emplois trouvés: " + emplois.size());
        emplois.forEach(e -> System.out.println(" - Emploi: " + e.getId() + ", matiere: " + (e.getMatiere() != null ? e.getMatiere().getNom() : "null") + ", classe: " + (e.getClasse() != null ? e.getClasse().getNom() : "null") + ", heureDebut: " + e.getHeureDebut() + ", heureFin: " + e.getHeureFin()));

        // 1. Trouver la séance en cours ou la prochaine
        var emploiSelectionne = emplois.stream()
                .filter(e -> e.getHeureDebut() != null && e.getHeureFin() != null
                        && !maintenant.isBefore(e.getHeureDebut())
                        && !maintenant.isAfter(e.getHeureFin()))
                .findFirst();
        System.out.println("emploiSelectionne (en cours): " + (emploiSelectionne.isPresent() ? "trouvé" : "non trouvé"));

        if (emploiSelectionne.isEmpty()) {
            emploiSelectionne = emplois.stream()
                    .filter(e -> e.getHeureDebut() != null && e.getHeureDebut().isAfter(maintenant))
                    .findFirst();
            System.out.println("emploiSelectionne (prochaine): " + (emploiSelectionne.isPresent() ? "trouvé" : "non trouvé"));
        }

        if (emploiSelectionne.isEmpty()) {
            // Si pas de séance en cours ou à venir, prendre la dernière séance terminée du jour
            emploiSelectionne = emplois.stream()
                    .filter(e -> e.getHeureDebut() != null && e.getHeureFin() != null && e.getHeureFin().isBefore(maintenant))
                    .max(java.util.Comparator.comparing(EmploiDuTemps::getHeureFin));
            System.out.println("emploiSelectionne (dernière terminée): " + (emploiSelectionne.isPresent() ? "trouvé" : "non trouvé"));
        }

        if (emploiSelectionne.isEmpty()) {
            System.out.println("Aucune séance trouvée !");
            return Map.of(
                    "enseignantId", enseignantId,
                    "date", aujourd_hui.toString(),
                    "message", "Aucune séance en cours ou prochaine aujourd'hui pour cet enseignant"
            );
        }

        EmploiDuTemps emploi = emploiSelectionne.get();
        String matiere = emploi.getMatiere() != null ? emploi.getMatiere().getNom() : null;
        Classe classe = emploi.getClasse();
        System.out.println("emploi sélectionné: matiere=" + matiere + ", classe=" + (classe != null ? classe.getNom() : "null"));

        if (matiere == null || matiere.isBlank() || classe == null) {
            System.out.println("matiere ou classe null/vide !");
            return Map.of(
                    "enseignantId", enseignantId,
                    "date", aujourd_hui.toString(),
                    "message", "Impossible de déterminer la matière ou la classe de la séance"
            );
        }

        // 2. Récupérer toutes les notes pour cette enseignant, cette matière et cette classe
        List<Note> notes = noteRepository.findByEnseignantMatiereClasse(enseignantId, matiere, classe.getId());
        System.out.println("notes trouvées: " + notes.size());

        if (notes.isEmpty()) {
            System.out.println("Aucune note trouvée !");
            return Map.of(
                    "enseignantId", enseignantId,
                    "date", aujourd_hui.toString(),
                    "matiere", matiere,
                    "classeId", classe.getId(),
                    "classeNom", classe.getNom(),
                    "message", "Aucune note disponible pour cette classe et cette matière"
            );
        }

        // 3. Calculer les 4 stats
        double meilleureNote = notes.stream().mapToDouble(Note::getValeur).max().orElse(0);
        double plusFaibleNote = notes.stream().mapToDouble(Note::getValeur).min().orElse(0);
        long notesSuperieuresOuEgalesA10 = notes.stream().filter(n -> n.getValeur() >= 10).count();
        double tauxReussite = (notesSuperieuresOuEgalesA10 * 100.0) / notes.size();
        int nombreEtudiants = (classe.getInscriptions() != null) ? classe.getInscriptions().size() : 0;

        Map<String, Object> result = Map.of(
                "enseignantId", enseignantId,
                "date", aujourd_hui.toString(),
                "matiere", matiere,
                "classeId", classe.getId(),
                "classeNom", classe.getNom(),
                "nombreEtudiants", nombreEtudiants,
                "tauxReussite", String.format("%.0f%%", tauxReussite),
                "meilleureNote", String.format("%.1f", meilleureNote),
                "plusFaibleNote", String.format("%.1f", plusFaibleNote),
                "notesSuperieuresOuEgalesA10", notesSuperieuresOuEgalesA10
        );

        System.out.println("=== Résultat renvoyé par le backend ===");
        System.out.println(result);

        return result;
    }

    public Map<String, Object> getTempsAvantProchaineSeanceParEnseignant(Long enseignantId) {
        LocalDate aujourd_hui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();

        return emploiDuTempsService.getByEnseignantAndDate(enseignantId, aujourd_hui).stream()
                .filter(s -> s.getHeureDebut() != null &&
                        (s.getHeureDebut().isAfter(maintenant) || s.getHeureDebut().equals(maintenant)))
                .min(Comparator.comparing(EmploiDuTemps::getHeureDebut))
                .map(prochaine -> {
                    Duration duree = Duration.between(maintenant, prochaine.getHeureDebut());
                    return Map.<String, Object>of(
                            "seanceId", prochaine.getId(),
                            "matiere", prochaine.getMatiere() != null ? prochaine.getMatiere().getNom() : null,
                            "heureDebut", prochaine.getHeureDebut().toString(),
                            "heureFin", prochaine.getHeureFin() != null ? prochaine.getHeureFin().toString() : null,
                            "minutesRestantes", duree.toMinutes(),
                            "heuresRestantes", duree.toHours(),
                            "statut", "PLANIFIEE"
                    );
                })
                .orElse(Map.of("message", "Aucune séance restante aujourd'hui pour cet enseignant"));
    }
}