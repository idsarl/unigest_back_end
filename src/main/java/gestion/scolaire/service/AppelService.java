package gestion.scolaire.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.dto.AppelBatchRequest;
import gestion.scolaire.dto.AppelItem;
import gestion.scolaire.model.Appel;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Seance;
import gestion.scolaire.model.StatutPresence;
import gestion.scolaire.repository.AppelRepository;
import gestion.scolaire.repository.EtudiantRepository;
import gestion.scolaire.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AppelService {

    private final AppelRepository appelRepository;
    private final SeanceRepository seanceRepository;
    private final EtudiantRepository etudiantRepository;
    private final AnneeScolaireService anneeScolaireService;

    /**
     * Faire l'appel d'un étudiant dans une séance
     */
    public Appel faireAppel(
            Long seanceId,
            Long etudiantId,
            StatutPresence statut,
            int retard,
            String motif) {

        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        Optional<Appel> appelExistant = appelRepository.findBySeanceIdAndEtudiantId(seanceId, etudiantId);

        if (appelExistant.isPresent()) {
            throw new RuntimeException(
                    "Cet étudiant a déjà été enregistré pour cette séance");
        }

        Appel appel = new Appel();
        appel.setSeance(seance);
        appel.setEtudiant(etudiant);
        appel.setStatut(statut);
        appel.setMinutesRetard(retard);
        appel.setMotif(motif);
        appel.setJustifie(false);

        return appelRepository.save(appel);
    }

    @Transactional
    public void faireAppelBatch(AppelBatchRequest request) {

        if (request.getAppels() == null || request.getAppels().isEmpty()) {
            return;
        }

        Seance seance = seanceRepository.findById(request.getSeanceId())
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));

        List<Appel> appels = new ArrayList<>();

        for (AppelItem item : request.getAppels()) {

            Optional<Etudiant> optEtudiant = etudiantRepository.findById(item.getEtudiantId());

            if (optEtudiant.isEmpty())
                continue;

            Etudiant etudiant = optEtudiant.get();

            boolean exists = appelRepository
                    .findBySeanceIdAndEtudiantId(request.getSeanceId(), etudiant.getId())
                    .isPresent();

            if (exists)
                continue;

            Appel appel = new Appel();
            appel.setSeance(seance);
            appel.setEtudiant(etudiant);
            appel.setStatut(item.getStatut());
            appel.setMinutesRetard(item.getMinutesRetard());
            appel.setMotif(item.getMotif());
            appel.setJustifie(false);

            appels.add(appel);
        }

        appelRepository.saveAll(appels);
    }

    /**
     * Modifier un appel
     */
    public Appel modifierAppel(
            Long appelId,
            StatutPresence statut,
            int retard,
            String motif) {

        Appel appel = appelRepository.findById(appelId)
                .orElseThrow(() -> new RuntimeException("Appel introuvable"));

        appel.setStatut(statut);
        appel.setMinutesRetard(retard);
        appel.setMotif(motif);

        return appelRepository.save(appel);
    }

    /**
     * Justifier une absence
     */
    public Appel justifierAbsence(
            Long appelId,
            String motif) {

        Appel appel = appelRepository.findById(appelId)
                .orElseThrow(() -> new RuntimeException("Appel introuvable"));

        appel.setJustifie(true);
        appel.setMotif(motif);
        appel.setDateJustification(LocalDateTime.now());

        return appelRepository.save(appel);
    }

    /**
     * Récupérer tous les appels d'une année scolaire
     */
    public List<Appel> getAppelsParAnnee(Long anneeId) {
        return appelRepository.findBySeanceAnneeScolaireId(anneeId);
    }

    /**
     * Récupérer tous les appels de l'année scolaire active
     */
    @Transactional(readOnly = true)
    public List<Appel> getAppels() {
        return appelRepository.findBySeanceAnneeScolaireId(anneeActiveId());
    }

    /**
     * Récupérer les appels d'une séance
     */
    @Transactional(readOnly = true)
    public List<Appel> getAppelsParSeance(Long seanceId) {
        return appelRepository.findBySeanceId(seanceId);
    }

    /**
     * Récupérer les appels d'un étudiant pour l'année scolaire active
     */
    @Transactional(readOnly = true)
    public List<Appel> getAppelsParEtudiant(Long etudiantId) {
        return appelRepository.findByEtudiantIdAndSeanceAnneeScolaireId(etudiantId, anneeActiveId());
    }

    /**
     * Récupérer l'appel d'un étudiant dans une séance
     */
    @Transactional(readOnly = true)
    public Appel getAppelEtudiantDansSeance(
            Long seanceId,
            Long etudiantId) {

        return appelRepository
                .findBySeanceIdAndEtudiantId(seanceId, etudiantId)
                .orElseThrow(() -> new RuntimeException("Appel introuvable"));
    }

    /**
     * Récupérer un appel par son id
     */
    @Transactional(readOnly = true)
    public Appel getAppelById(Long appelId) {
        return appelRepository.findById(appelId)
                .orElseThrow(() -> new RuntimeException("Appel introuvable"));
    }

    /**
     * Supprimer un appel
     */
    public void supprimerAppel(Long appelId) {

        if (!appelRepository.existsById(appelId)) {
            throw new RuntimeException("Appel introuvable");
        }

        appelRepository.deleteById(appelId);
    }

    /**
     * Supprimer l'appel d'un étudiant dans une séance
     */
    public void supprimerAppelEtudiantSeance(
            Long seanceId,
            Long etudiantId) {

        Appel appel = appelRepository
                .findBySeanceIdAndEtudiantId(seanceId, etudiantId)
                .orElseThrow(() -> new RuntimeException("Appel introuvable"));

        appelRepository.delete(appel);
    }

    /**
     * Supprimer tous les appels d'une séance
     */
    public void supprimerAppelsSeance(Long seanceId) {
        appelRepository.deleteBySeanceId(seanceId);
    }

    private Long anneeActiveId() {
        return anneeScolaireService.getAnneeActive().getId();
    }
}
