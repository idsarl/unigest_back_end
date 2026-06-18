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
    private final ParametreEcoleService parametreEcoleService;

    /** Types de notes qui composent la "note de classe" */
    private static final Set<TypeNote> TYPES_NOTE_CLASSE = Set.of(
            TypeNote.DEVOIR, TypeNote.INTERROGATION, TypeNote.PARTICIPATION, TypeNote.TP);

    /** Types de notes qui composent la "note de composition" */
    private static final Set<TypeNote> TYPES_NOTE_COMPO = Set.of(
            TypeNote.COMPOSITION, TypeNote.EXAMEN);

    // ─────────────────────────────────────────────────────────────────────────
    // Génération
    // ─────────────────────────────────────────────────────────────────────────

    public Bulletin genererBulletin(Long etudiantId, Integer periode, TypePeriode typePeriode) {

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

        Inscription inscriptionActive = etudiant.getInscription()
                .stream()
                .filter(i -> i.getAnneeScolaire().getId().equals(anneeActive.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucune inscription active trouvée"));

        Classe classe = inscriptionActive.getClasse();

        bulletinRepository
                .findByEtudiantIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        etudiantId, anneeActive.getId(), periode, typePeriode)
                .ifPresent(b -> {
                    throw new RuntimeException("Le bulletin existe déjà pour cette période");
                });

        ResultatCalcul resultat = calculerMoyennesEtLignes(
                etudiantId, classe.getId(), anneeActive.getId(), periode, typePeriode);

        Bulletin bulletin = new Bulletin();
        bulletin.setEtudiant(etudiant);
        bulletin.setClasse(classe);
        bulletin.setAnneeScolaire(anneeActive);
        bulletin.setPeriode(periode);
        bulletin.setTypePeriode(typePeriode);
        bulletin.setMoyenneGenerale(arrondir2(resultat.moyenneGenerale));
        bulletin.setAppreciation(parametreEcoleService.calculerAppreciation(resultat.moyenneGenerale));
        bulletin.setDateGeneration(LocalDate.now());

        Bulletin saved = bulletinRepository.save(bulletin);

        for (LigneBulletin ligne : resultat.lignes) {
            ligne.setBulletin(saved);
        }
        ligneBulletinRepository.saveAll(resultat.lignes);
        saved.setLignes(resultat.lignes);

        recalculerRangs(classe.getId(), anneeActive.getId(), periode, typePeriode);

        return saved;
    }

    public Bulletin regenererBulletin(Long etudiantId, Integer periode, TypePeriode typePeriode) {

        Bulletin bulletin = getBulletinEtudiantPeriode(etudiantId, periode, typePeriode);
        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

        ResultatCalcul resultat = calculerMoyennesEtLignes(
                etudiantId, bulletin.getClasse().getId(),
                anneeActive.getId(), periode, typePeriode);

        ligneBulletinRepository.deleteAll(ligneBulletinRepository.findByBulletin(bulletin));

        bulletin.setMoyenneGenerale(arrondir2(resultat.moyenneGenerale));
        bulletin.setAppreciation(parametreEcoleService.calculerAppreciation(resultat.moyenneGenerale));
        bulletin.setDateGeneration(LocalDate.now());
        Bulletin saved = bulletinRepository.save(bulletin);

        for (LigneBulletin ligne : resultat.lignes) {
            ligne.setBulletin(saved);
        }
        ligneBulletinRepository.saveAll(resultat.lignes);
        saved.setLignes(resultat.lignes);

        recalculerRangs(bulletin.getClasse().getId(), anneeActive.getId(), periode, typePeriode);

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Calcul central
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Algorithme :
     *  - Note de classe   = moyenne(DEVOIR + INTERROGATION + PARTICIPATION + TP)
     *  - Note de compo    = moyenne(COMPOSITION + EXAMEN)
     *  - Moyenne matière  = (noteClasse × quotaClasse%) + (noteCompo × quotaCompo%)
     *    Si seules les notes de classe existent → moyenne = noteClasse (100 %)
     *    Si seule la note de composition existe → moyenne = noteCompo  (100 %)
     *  - Moyenne générale = Σ(moyenneMatière × coeff) / Σ(coeff)
     */
    private ResultatCalcul calculerMoyennesEtLignes(
            Long etudiantId, Long classeId, Long anneeId,
            Integer periode, TypePeriode typePeriode) {

        List<Note> notes = noteService.getNotesEtudiantPeriode(etudiantId, periode, typePeriode);

        Map<Long, List<Note>> parMatiere = notes.stream()
                .collect(Collectors.groupingBy(n -> n.getMatiere().getId()));

        List<ClasseMatiere> classeMatieres = classeMatiereRepository.findByClasseId(classeId);

        ParametreEcole parametre = parametreEcoleService.getParametres();
        double quotaClasse = parametre.getQuotaClasse() / 100.0;
        double quotaCompo  = parametre.getQuotaComposition() / 100.0;

        List<LigneBulletin> lignes = new ArrayList<>();
        double totalPondere = 0;
        double totalCoeff   = 0;

        for (ClasseMatiere cm : classeMatieres) {
            Long matiereId = cm.getMatiere().getId();
            List<Note> notesMatiere = parMatiere.getOrDefault(matiereId, Collections.emptyList());

            if (notesMatiere.isEmpty()) continue;

            // Séparer notes de classe et notes de composition
            List<Note> notesClasse = notesMatiere.stream()
                    .filter(n -> TYPES_NOTE_CLASSE.contains(n.getType()))
                    .collect(Collectors.toList());

            List<Note> notesCompo = notesMatiere.stream()
                    .filter(n -> TYPES_NOTE_COMPO.contains(n.getType()))
                    .collect(Collectors.toList());

            double noteClasse = notesClasse.isEmpty() ? 0 :
                    notesClasse.stream().mapToDouble(Note::getValeur).average().orElse(0);

            double noteCompo = notesCompo.isEmpty() ? 0 :
                    notesCompo.stream().mapToDouble(Note::getValeur).average().orElse(0);

            double moyenneMatiere;
            if (!notesClasse.isEmpty() && !notesCompo.isEmpty()) {
                moyenneMatiere = (noteClasse * quotaClasse) + (noteCompo * quotaCompo);
            } else if (!notesClasse.isEmpty()) {
                moyenneMatiere = noteClasse;
            } else {
                moyenneMatiere = noteCompo;
            }

            double coeff = cm.getCoefficient();
            totalPondere += moyenneMatiere * coeff;
            totalCoeff   += coeff;

            LigneBulletin ligne = new LigneBulletin();
            ligne.setMatiere(cm.getMatiere());
            ligne.setNoteClasse(arrondir2(noteClasse));
            ligne.setNoteComposition(arrondir2(noteCompo));
            ligne.setQuotaClasse(parametre.getQuotaClasse());
            ligne.setQuotaComposition(parametre.getQuotaComposition());
            ligne.setMoyenneMatiere(arrondir2(moyenneMatiere));
            ligne.setCoefficient(coeff);
            ligne.setAppreciation(parametreEcoleService.calculerAppreciation(moyenneMatiere));
            lignes.add(ligne);
        }

        double moyenneGenerale = (totalCoeff > 0) ? (totalPondere / totalCoeff) : 0;

        return new ResultatCalcul(moyenneGenerale, lignes);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rang
    // ─────────────────────────────────────────────────────────────────────────

    private void recalculerRangs(Long classeId, Long anneeId, Integer periode, TypePeriode typePeriode) {

        List<Bulletin> bulletins = bulletinRepository
                .findByClasseIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        classeId, anneeId, periode, typePeriode);

        bulletins.sort(Comparator.comparingDouble(Bulletin::getMoyenneGenerale).reversed());

        for (int i = 0; i < bulletins.size(); i++) {
            bulletins.get(i).setRang(i + 1);
        }

        bulletinRepository.saveAll(bulletins);
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
    public Bulletin getBulletinEtudiantPeriode(Long etudiantId, Integer periode, TypePeriode typePeriode) {
        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
        return bulletinRepository
                .findByEtudiantIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        etudiantId, anneeActive.getId(), periode, typePeriode)
                .orElseThrow(() -> new RuntimeException("Bulletin introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Bulletin> getBulletinsClasse(Long classeId) {
        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
        return bulletinRepository.findByClasseIdAndAnneeScolaireId(classeId, anneeActive.getId());
    }

    @Transactional(readOnly = true)
    public List<Bulletin> getBulletinsClassePeriode(Long classeId, Integer periode, TypePeriode typePeriode) {
        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
        return bulletinRepository
                .findByClasseIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        classeId, anneeActive.getId(), periode, typePeriode);
    }

    public void supprimerBulletin(Long bulletinId) {
        bulletinRepository.delete(getBulletin(bulletinId));
    }

    /** Moyenne du 1er de la classe et effectif total pour la période. */
    @Transactional(readOnly = true)
    public StatistiquesClasse getStatistiquesClasse(
            Long classeId, Long anneeId, Integer periode, TypePeriode typePeriode) {

        List<Bulletin> bulletins = bulletinRepository
                .findByClasseIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                        classeId, anneeId, periode, typePeriode);

        if (bulletins.isEmpty()) return new StatistiquesClasse(0, 0);

        double moyennePremier = bulletins.stream()
                .mapToDouble(Bulletin::getMoyenneGenerale)
                .max().orElse(0);

        return new StatistiquesClasse(moyennePremier, bulletins.size());
    }

    public record StatistiquesClasse(double moyennePremier, int effectif) {}

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaires
    // ─────────────────────────────────────────────────────────────────────────

    private double arrondir2(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }

    private static class ResultatCalcul {
        final double moyenneGenerale;
        final List<LigneBulletin> lignes;

        ResultatCalcul(double moyenneGenerale, List<LigneBulletin> lignes) {
            this.moyenneGenerale = moyenneGenerale;
            this.lignes = lignes;
        }
    }
}
