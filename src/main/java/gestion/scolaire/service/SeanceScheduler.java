package gestion.scolaire.service;

import gestion.scolaire.model.*;
import gestion.scolaire.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeanceScheduler {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final SeanceRepository seanceRepository;
    private final AffectationRepository affectationRepository;
    private final AnneeScolaireService anneeScolaireService;

    @Scheduled(cron = "0 1 0 * * ?") // S'exécute à 00:01 chaque jour
    public void genererSeancesDuJour() {
        System.out.println("Début de la génération automatique des séances du jour...");
        LocalDate aujourdHui = LocalDate.now();
        List<EmploiDuTemps> emploisDuJour = emploiDuTempsRepository.findAllValides(aujourdHui)
                .stream()
                .filter(e -> {
                    if (e.getJours() == null) return false;
                    gestion.scolaire.dto.JourSemaine todayJour = gestion.scolaire.dto.JourSemaine.values()[aujourdHui.getDayOfWeek().getValue() - 1];
                    return e.getJours().contains(todayJour);
                })
                .filter(e -> e.getType() == gestion.scolaire.dto.TypeEmploi.COURS)
                .toList();

        System.out.println(emploisDuJour.size() + " emplois du temps trouvés pour " + aujourdHui.getDayOfWeek());

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
                System.out.println("Aucune affectation trouvée pour Classe " + emploi.getClasse().getNom() + 
                                   " et Enseignant " + emploi.getEnseignant().getId() + 
                                   " en " + emploi.getMatiere().getNom());
                continue;
            }

            Affectation affectation = affectationOpt.get();

            // Vérifier si la séance existe déjà pour éviter les doublons
            List<Seance> existantes = seanceRepository.findByAffectationIdAndDate(affectation.getId(), aujourdHui);
            boolean seanceExiste = existantes.stream()
                    .anyMatch(s -> s.getMatiere().equals(emploi.getMatiere().getNom()) && 
                                   s.getHeureDebut().equals(emploi.getHeureDebut()));

            if (!seanceExiste) {
                AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
                Seance seance = new Seance();
                seance.setAffectation(affectation);
                seance.setAnneeScolaire(anneeActive);
                seance.setMatiere(emploi.getMatiere().getNom());
                seance.setDate(aujourdHui);
                seance.setHeureDebut(emploi.getHeureDebut());
                seance.setHeureFin(emploi.getHeureFin()); // Facultatif, selon le besoin
                seance.setStatut(StatutSeance.PLANIFIEE);
                seance.setDateCreation(LocalDateTime.now());
                seance.setDateModification(LocalDateTime.now());

                seanceRepository.save(seance);
                System.out.println("Séance générée : " + affectation.getId() + " - " + emploi.getMatiere().getNom());
            }
        }
        System.out.println("Fin de la génération automatique des séances du jour.");
    }

    @Scheduled(fixedRate = 60000) // S'exécute chaque minute
    public void mettreAJourSeancesNonEffectuees() {
        LocalDate aujourdHui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();

        List<Seance> seancesPassees = seanceRepository.findPastSeancesByStatut(
                StatutSeance.PLANIFIEE, 
                aujourdHui, 
                maintenant
        );

        if (!seancesPassees.isEmpty()) {
            System.out.println(seancesPassees.size() + " séances planifiées sont dépassées et seront marquées comme NON_EFFECTUEE.");
            for (Seance seance : seancesPassees) {
                seance.setStatut(StatutSeance.NON_EFFECTUEE);
                seance.setDateModification(LocalDateTime.now());
                seanceRepository.save(seance);
                System.out.println("Séance " + seance.getId() + " marquée comme NON_EFFECTUEE.");
            }
        }
    }
}
