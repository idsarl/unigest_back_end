package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.model.*;
import gestion.scolaire.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BulletinService {

    private final NoteService noteService;
    private final BulletinRepository bulletinRepository;
    private final LigneBulletinRepository ligneBulletinRepository;
    private final ClasseMatiereRepository classeMatiereRepository;
    private final EtudiantRepository etudiantRepository;
    private final AnneeScolaireService anneeScolaireService;

    // ─────────────────────────────────────────────────────────────────────────
    // Génération
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Générer le bulletin d'un étudiant pour une période donnée.
     *
     * Algorithme :
     *  1. Récupérer les notes de l'étudiant pour la période
     *  2. Grouper par matière → calculer la moyenne simple par matière
     *  3. Appliquer le coefficient (ClasseMatiere) → moyenne pondérée générale
     *  4. Créer les LigneBulletin (une par matière)
     *  5. Calculer l'appréciation
     *  6. Recalculer les rangs de toute la classe
     */
    public Bulletin genererBulletin(
            Long etudiantId,
            Integer periode,
            TypePeriode typePeriode) {

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

        Inscription inscriptionActive = etudiant.getInscription()
                .stream()
                .filter(i -> i.getAnneeScolaire().getId().equals(anneeActive.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucune inscription active trouvée"));

        Classe classe = inscriptionActive.getClasse();

        // Empêcher les doublons
        bulletinRepository
                .findByEtudiantIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        etudiantId, anneeActive.getId(), periode, typePeriode)
                .ifPresent(b -> {
                    throw new RuntimeException("Le bulletin existe déjà pour cette période");
                });

        // Calcul des lignes + moyenne générale
        ResultatCalcul resultat = calculerMoyennesEtLignes(
                etudiantId, classe.getId(), anneeActive.getId(), periode, typePeriode);

        // Créer le bulletin
        Bulletin bulletin = new Bulletin();
        bulletin.setEtudiant(etudiant);
        bulletin.setClasse(classe);
        bulletin.setAnneeScolaire(anneeActive);
        bulletin.setPeriode(periode);
        bulletin.setTypePeriode(typePeriode);
        bulletin.setMoyenneGenerale(arrondir2(resultat.moyenneGenerale));
        bulletin.setAppreciation(calculerAppreciation(resultat.moyenneGenerale));
        bulletin.setDateGeneration(LocalDate.now());

        Bulletin saved = bulletinRepository.save(bulletin);

        // Sauvegarder les lignes par matière
        for (LigneBulletin ligne : resultat.lignes) {
            ligne.setBulletin(saved);
        }
        ligneBulletinRepository.saveAll(resultat.lignes);
        saved.setLignes(resultat.lignes);

        // Recalculer les rangs de toute la classe pour cette période
        recalculerRangs(classe.getId(), anneeActive.getId(), periode, typePeriode);

        return saved;
    }

    /**
     * Régénérer un bulletin existant (notes modifiées ou ajoutées après coup).
     */
    public Bulletin regenererBulletin(
            Long etudiantId,
            Integer periode,
            TypePeriode typePeriode) {

        Bulletin bulletin = getBulletinEtudiantPeriode(etudiantId, periode, typePeriode);
        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

        ResultatCalcul resultat = calculerMoyennesEtLignes(
                etudiantId,
                bulletin.getClasse().getId(),
                anneeActive.getId(),
                periode,
                typePeriode);

        // Supprimer les anciennes lignes
        List<LigneBulletin> anciennesLignes = ligneBulletinRepository.findByBulletin(bulletin);
        ligneBulletinRepository.deleteAll(anciennesLignes);

        // Mettre à jour le bulletin
        bulletin.setMoyenneGenerale(arrondir2(resultat.moyenneGenerale));
        bulletin.setAppreciation(calculerAppreciation(resultat.moyenneGenerale));
        bulletin.setDateGeneration(LocalDate.now());
        Bulletin saved = bulletinRepository.save(bulletin);

        // Sauvegarder les nouvelles lignes
        for (LigneBulletin ligne : resultat.lignes) {
            ligne.setBulletin(saved);
        }
        ligneBulletinRepository.saveAll(resultat.lignes);
        saved.setLignes(resultat.lignes);

        // Recalculer les rangs
        recalculerRangs(
                bulletin.getClasse().getId(),
                anneeActive.getId(),
                periode,
                typePeriode);

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Calcul central
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcule la moyenne générale et construit les LigneBulletin.
     *
     * Formule :
     *   moyenneMatiere  = SUM(valeurs) / nb_notes_de_cette_matiere
     *   moyenneGenerale = SUM(moyenneMatiere × coeff) / SUM(coeff)
     *
     * Seules les matières ayant au moins une note sont incluses.
     */
    private ResultatCalcul calculerMoyennesEtLignes(
            Long etudiantId,
            Long classeId,
            Long anneeId,
            Integer periode,
            TypePeriode typePeriode) {

        // Notes de l'étudiant pour la période
        List<Note> notes = noteService.getNotesEtudiantPeriode(etudiantId, periode, typePeriode);

        // Regrouper par matière : matiereId → liste de notes
        Map<Long, List<Note>> parMatiere = notes.stream()
                .collect(Collectors.groupingBy(n -> n.getMatiere().getId()));

        // Coefficients définis pour la classe
        List<ClasseMatiere> classeMatieres = classeMatiereRepository.findByClasseId(classeId);

        List<LigneBulletin> lignes = new ArrayList<>();
        double totalPondere = 0;
        double totalCoeff   = 0;

        for (ClasseMatiere cm : classeMatieres) {
            Long matiereId = cm.getMatiere().getId();
            List<Note> notesMatiere = parMatiere.getOrDefault(matiereId, Collections.emptyList());

            if (notesMatiere.isEmpty()) {
                // Pas de notes saisies pour cette matière → on l'ignore
                continue;
            }

            // Moyenne simple des notes de cette matière
            double moyenneMatiere = notesMatiere.stream()
                    .mapToDouble(Note::getValeur)
                    .average()
                    .orElse(0);

            double coeff = cm.getCoefficient();
            totalPondere += moyenneMatiere * coeff;
            totalCoeff   += coeff;

            LigneBulletin ligne = new LigneBulletin();
            ligne.setMatiere(cm.getMatiere());
            ligne.setMoyenneMatiere(arrondir2(moyenneMatiere));
            ligne.setCoefficient(coeff);
            ligne.setAppreciation(calculerAppreciation(moyenneMatiere));
            lignes.add(ligne);
        }

        double moyenneGenerale = (totalCoeff > 0) ? (totalPondere / totalCoeff) : 0;

        return new ResultatCalcul(moyenneGenerale, lignes);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rang
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Recalcule et met à jour le rang de chaque étudiant dans la classe
     * pour la période donnée. Tri par moyenne décroissante.
     */
    private void recalculerRangs(
            Long classeId,
            Long anneeId,
            Integer periode,
            TypePeriode typePeriode) {

        List<Bulletin> bulletins = bulletinRepository
                .findByClasseIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        classeId, anneeId, periode, typePeriode);

        bulletins.sort(
                Comparator.comparingDouble(Bulletin::getMoyenneGenerale).reversed());

        for (int i = 0; i < bulletins.size(); i++) {
            bulletins.get(i).setRang(i + 1);
        }

        bulletinRepository.saveAll(bulletins);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Appréciation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Traduit une moyenne en appréciation textuelle.
     * Barème standard : 0–9.99 Insuffisant | 10–11.99 Passable |
     *                   12–13.99 Assez bien | 14–15.99 Bien |
     *                   16–17.99 Très bien  | 18–20 Excellent
     */
    public static String calculerAppreciation(double moyenne) {
        if (moyenne >= 18) return "Excellent";
        if (moyenne >= 16) return "Très bien";
        if (moyenne >= 14) return "Bien";
        if (moyenne >= 12) return "Assez bien";
        if (moyenne >= 10) return "Passable";
        if (moyenne >=  5) return "Insuffisant";
        return "Très insuffisant";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Bulletin getBulletin(Long bulletinId) {
        return bulletinRepository.findById(bulletinId)
                .orElseThrow(() -> new RuntimeException("Bulletin introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Bulletin> getBulletinsEtudiant(Long etudiantId) {
        return bulletinRepository.findByEtudiantId(etudiantId);
    }

    @Transactional(readOnly = true)
    public Bulletin getBulletinEtudiantPeriode(
            Long etudiantId, Integer periode, TypePeriode typePeriode) {

        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
        return bulletinRepository
                .findByEtudiantIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        etudiantId, anneeActive.getId(), periode, typePeriode)
                .orElseThrow(() -> new RuntimeException("Bulletin introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Bulletin> getBulletinsClasse(Long classeId) {
        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
        return bulletinRepository.findByClasseIdAndAnneeScolaireId(
                classeId, anneeActive.getId());
    }

    @Transactional(readOnly = true)
    public List<Bulletin> getBulletinsClassePeriode(
            Long classeId, Integer periode, TypePeriode typePeriode) {

        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
        return bulletinRepository
                .findByClasseIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        classeId, anneeActive.getId(), periode, typePeriode);
    }

    public void supprimerBulletin(Long bulletinId) {
        bulletinRepository.delete(getBulletin(bulletinId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaires
    // ─────────────────────────────────────────────────────────────────────────

    private double arrondir2(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }

    /** DTO interne pour transporter le résultat du calcul. */
    private static class ResultatCalcul {
        final double moyenneGenerale;
        final List<LigneBulletin> lignes;

        ResultatCalcul(double moyenneGenerale, List<LigneBulletin> lignes) {
            this.moyenneGenerale = moyenneGenerale;
            this.lignes = lignes;
        }
    }
}
