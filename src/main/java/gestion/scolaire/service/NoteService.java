package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.dto.NoteBatchRequest;
import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.ClasseMatiere;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Matiere;
import gestion.scolaire.model.Note;
import gestion.scolaire.model.TypeNote;
import gestion.scolaire.model.TypePeriode;
import gestion.scolaire.repository.AffectationRepository;
import gestion.scolaire.repository.ClasseMatiereRepository;
import gestion.scolaire.repository.EtudiantRepository;
import gestion.scolaire.repository.MatiereRepository;
import gestion.scolaire.repository.NoteRepository;
import lombok.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

        private final NoteRepository noteRepository;
        private final EtudiantRepository etudiantRepository;
        private final AffectationRepository affectationRepository;
        private final MatiereRepository matiereRepository;
        private final ClasseMatiereRepository classeMatiereRepository;
        private final AnneeScolaireService anneeScolaireService;

        /**
         * Ajouter une note 
         */
        @Transactional
        public Note ajouterNote(
                        Long etudiantId,
                        Long affectationId,
                        Long matiereId,
                        double valeur,
                        TypeNote type,
                        Integer periode,
                        TypePeriode typePeriode) {

                if (valeur < 0 || valeur > 20) {
                        throw new RuntimeException("La note doit être comprise entre 0 et 20");
                }

                Etudiant etudiant = etudiantRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

                Affectation affectation = affectationRepository.findById(affectationId)
                                .orElseThrow(() -> new RuntimeException("Affectation introuvable"));

                Matiere matiere = matiereRepository.findById(matiereId)
                                .orElseThrow(() -> new RuntimeException("Matière introuvable"));

                AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

                boolean existe = noteRepository
                                .existsByEtudiantIdAndAffectationIdAndAnneeScolaireIdAndMatiereIdAndPeriodeAndTypeAndTypePeriode(
                                                etudiantId,
                                                affectationId,
                                                anneeActive.getId(),
                                                matiereId,
                                                periode,
                                                type,
                                                typePeriode);

                if (existe) {
                        throw new RuntimeException("Cette note existe déjà pour cet étudiant");
                }

                ClasseMatiere classeMatiere = classeMatiereRepository
                                .findByClasseIdAndMatiereId(
                                                affectation.getClasse().getId(),
                                                matiereId)
                                .orElseThrow(() -> new RuntimeException("Coefficient introuvable"));

                Note note = new Note();
                note.setEtudiant(etudiant);
                note.setAffectation(affectation);
                note.setAnneeScolaire(anneeActive);
                note.setMatiere(matiere);
                note.setValeur(valeur);
                note.setCoefficient(classeMatiere.getCoefficient());
                note.setType(type);
                note.setPeriode(periode);
                note.setTypePeriode(typePeriode);
                note.setDateEvaluation(LocalDate.now());

                return noteRepository.save(note);
        }

        /**
         * Ajouter plusieurs notes
         */
        @Transactional
        public List<Note> ajouterNotesBatch(List<NoteBatchRequest> requests) {
                List<Note> notes = new ArrayList<>();

                for (NoteBatchRequest request : requests) {
                        Note note = ajouterNote(
                                        request.getEtudiantId(),
                                        request.getAffectationId(),
                                        request.getMatiereId(),
                                        request.getValeur(),
                                        request.getType(),
                                        request.getPeriode(),
                                        request.getTypePeriode());

                        notes.add(note);
                }

                return notes;
        }

        /**
         * Modifier une note
         */
        @Transactional
        public Note modifierNote(
                        Long noteId,
                        double valeur,
                        TypeNote type) {

                if (valeur < 0 || valeur > 20) {
                        throw new RuntimeException("La note doit être comprise entre 0 et 20");
                }

                Note note = noteRepository.findById(noteId)
                                .orElseThrow(() -> new RuntimeException("Note introuvable"));

                note.setValeur(valeur);
                note.setType(type);

                return noteRepository.save(note);
        }

        /**
         * Supprimer une note
         */
        @Transactional
        public void supprimerNote(Long noteId) {
                Note note = getNoteById(noteId);
                noteRepository.delete(note);
        }

        /**
         * Récupérer une note
         */
        public Note getNoteById(Long noteId) {
                return noteRepository.findById(noteId)
                                .orElseThrow(() -> new RuntimeException("Note introuvable"));
        }

        /**
         * Notes d’un étudiant
         */
        public List<Note> getNotesEtudiant(Long etudiantId) {
                AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

                return noteRepository.findByEtudiantIdAndAnneeScolaireId(
                                etudiantId,
                                anneeActive.getId());
        }

        /**
         * Notes par classe et période
         */
        // public List<Note> getNotesParClasseEtPeriode(
        //                 Long classeId,
        //                 Long anneeScolaireId,
        //                 Integer periode,
        //                 TypePeriode typePeriode) {

        //         return noteRepository.findByClasseAndPeriode(
        //                         classeId,
        //                         anneeScolaireId,
        //                         periode,
        //                         typePeriode);
        // }

        /**
         * Notes étudiant par période
         */
        public List<Note> getNotesEtudiantPeriode(
                        Long etudiantId,
                        Integer periode,
                        TypePeriode typePeriode) {

                AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

                return noteRepository
                                .findByEtudiantIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                                                etudiantId,
                                                anneeActive.getId(),
                                                periode,
                                                typePeriode);
        }

        /**
         * Notes par affectation
         */
        public List<Note> getNotesParAffectation(Long affectationId) {
                AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

                return noteRepository.findByAffectationIdAndAnneeScolaireId(
                                affectationId,
                                anneeActive.getId());
        }

        /**
         * Notes par période
         */
        public List<Note> getNotesParPeriode(
                        Long anneeScolaireId,
                        Integer periode,
                        TypePeriode typePeriode) {

                return noteRepository.findByAnneeScolaireIdAndPeriodeAndTypePeriode(
                                anneeScolaireId,
                                periode,
                                typePeriode);
        }

        /**
         * Notes période active
         */
        public List<Note> getNotesParPeriodeActive(
                        Integer periode,
                        TypePeriode typePeriode) {

                AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

                return noteRepository.findByAnneeScolaireIdAndPeriodeAndTypePeriode(
                                anneeActive.getId(),
                                periode,
                                typePeriode);
        }

        public List<Note> getNotesParClasseEtPeriode(
                        Long classeId,
                        Long anneeScolaireId,
                        Integer periode,
                        TypePeriode typePeriode) {

                return noteRepository.findByAffectationClasseIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                                classeId,
                                anneeScolaireId,
                                periode,
                                typePeriode);
        }

        /**
         * Notes par affectation et période
         */
        public List<Note> getNotesParAffectationEtPeriode(
                        Long affectationId,
                        Integer periode,
                        TypePeriode typePeriode) {

                AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();

                return noteRepository
                                .findByAffectationIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
                                                affectationId,
                                                anneeActive.getId(),
                                                periode,
                                                typePeriode);
        }

        /**
         * Moyenne pondérée étudiant
         */
        public double calculerMoyenneEtudiant(
                        Long etudiantId,
                        Integer periode,
                        TypePeriode typePeriode) {

                List<Note> notes = getNotesEtudiantPeriode(
                                etudiantId,
                                periode,
                                typePeriode);

                if (notes.isEmpty()) {
                        return 0;
                }

                double totalPoints = notes.stream()
                                .mapToDouble(note -> note.getValeur() * note.getCoefficient())
                                .sum();

                double totalCoefficients = notes.stream()
                                .mapToDouble(Note::getCoefficient)
                                .sum();

                if (totalCoefficients == 0) {
                        return 0;
                }

                return totalPoints / totalCoefficients;
        }
}