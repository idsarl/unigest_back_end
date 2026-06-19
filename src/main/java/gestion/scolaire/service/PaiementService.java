package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import gestion.scolaire.dto.PaiementResumeDTO;
import gestion.scolaire.model.Filiere;
import gestion.scolaire.model.Inscription;
import gestion.scolaire.model.ModePaiement;
import gestion.scolaire.model.Paiement;
import gestion.scolaire.repository.InscriptionRepository;
import gestion.scolaire.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final InscriptionRepository inscriptionRepository;
    private final AnneeScolaireService anneeScolaireService;

    // ─────────────────────────────────────────────────────────────────────────
    // Écriture
    // ─────────────────────────────────────────────────────────────────────────

    public Paiement effectuerPaiement(Long inscriptionId, double montant, ModePaiement mode) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));

        Paiement paiement = new Paiement();
        paiement.setInscription(inscription);
        paiement.setMontant(montant);
        paiement.setDatePaiement(LocalDate.now());
        paiement.setModePaiement(mode);
        paiement.setReference(genererRef());

        return paiementRepository.save(paiement);
    }

    public Paiement modifierPaiement(Long paiementId, Long inscriptionId, double montant, ModePaiement mode) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable"));

        paiement.setMontant(montant);
        paiement.setModePaiement(mode);
        if (inscriptionId != null) {
            Inscription inscription = inscriptionRepository.findById(inscriptionId)
                    .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
            paiement.setInscription(inscription);
        }

        return paiementRepository.save(paiement);
    }

    public void supprimerPaiement(Long paiementId) {
        paiementRepository.deleteById(paiementId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture — filtrées par année scolaire active
    // ─────────────────────────────────────────────────────────────────────────

    /** Tous les paiements de l'année scolaire active. */
    public List<Paiement> getAllPaiements() {
        return paiementRepository.findByAnneeId(anneeActiveId());
    }

    /** Paiements d'un étudiant pour l'année scolaire active. */
    public List<Paiement> getPaiementsParEtudiant(Long etudiantId) {
        return paiementRepository.findByEtudiantIdAndAnneeId(etudiantId, anneeActiveId());
    }

    /** Historique des paiements d'un étudiant dans une classe et année spécifiques. */
    public List<Paiement> getHistoriquePaiementsEtudiant(Long etudiantId, Long classeId, Long anneeId) {
        return paiementRepository.findByEtudiantClasseAnnee(etudiantId, classeId, anneeId);
    }

    /** Historique des paiements par classe et année scolaire (pour rapports). */
    public List<Paiement> getHistoriquePaiements(Long classeId, Long anneeId) {
        return paiementRepository.findByClasseAndAnnee(classeId, anneeId);
    }

    public Paiement getPaiementById(Long paiementId) {
        return paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Calculs
    // ─────────────────────────────────────────────────────────────────────────

    public double calculerTotalPaye(Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
        Double total = paiementRepository.sumMontantByInscription(inscription);
        return total == null ? 0 : total;
    }

    public PaiementResumeDTO calculerResume(Inscription inscription) {
        Filiere filiere = inscription.getClasse().getFiliere();

        double fraisInscription = filiere.getFraisInscription();
        double fraisScolarite   = filiere.getFraisScolarite();
        double totalBrut        = fraisInscription + fraisScolarite;
        double reduction        = inscription.getMontantReduction();
        double totalNet         = totalBrut - reduction;
        double totalPaye        = inscription.getPaiements()
                .stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
        double reste = totalNet - totalPaye;

        PaiementResumeDTO dto = new PaiementResumeDTO();
        dto.setTotalBrut(totalBrut);
        dto.setReduction(reduction);
        dto.setTotalNet(totalNet);
        dto.setTotalPaye(totalPaye);
        dto.setResteAPayer(reste);

        if (reste <= 0) {
            dto.setStatutPaiement("COMPLET");
        } else if (totalPaye > 0) {
            dto.setStatutPaiement("PARTIEL");
        } else {
            dto.setStatutPaiement("IMPAYE");
        }

        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String genererRef() {
        int annee  = LocalDate.now().getYear();
        long count = paiementRepository.count();
        return "PAI-" + annee + "-" + String.format("%04d", count + 1);
    }

    private Long anneeActiveId() {
        return anneeScolaireService.getAnneeActive().getId();
    }
}
