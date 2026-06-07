package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.scolaire.dto.AffectationDTO;
import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Classe;
import gestion.scolaire.model.Enseignant;
import gestion.scolaire.model.Matiere;
import gestion.scolaire.repository.AffectationRepository;
import gestion.scolaire.repository.ClasseRepository;
import gestion.scolaire.repository.EnseignantRepository;
import gestion.scolaire.repository.MatiereRepository;

@Service
public class AffectationService {

    @Autowired
    private AffectationRepository affectationRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private MatiereRepository matiereRepository;
    @Autowired
    private  AnneeScolaireService anneeScolaireService;

    // 1️⃣ Ajouter une affectation
    public Affectation ajouterAffectation(Long enseignantId,
                                      List<Long> matiereIds,
                                      Long classeId) {

    Enseignant enseignant = enseignantRepository.findById(enseignantId)
            .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));

    List<Matiere> matieres = matiereRepository.findAllById(matiereIds);

    if (matieres.isEmpty()) {
        throw new RuntimeException("Aucune matière trouvée");
    }

    Classe classe = classeRepository.findById(classeId)
            .orElseThrow(() -> new RuntimeException("Classe introuvable"));

    AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
    Affectation affectation = new Affectation();
    affectation.setAnneeScolaire(anneeActive);
    affectation.setEnseignant(enseignant);
    affectation.setMatieres(matieres); // ✅ IMPORTANT
    affectation.setClasse(classe);
    affectation.setDateCreation(LocalDate.now());

    return affectationRepository.save(affectation);
}

    // 2️⃣ Modifier une affectation
   public Affectation modifierAffectation(Long affectationId,
                                       Long enseignantId,
                                       List<Long> matiereIds,
                                       Long classeId) {

    Affectation affectation = affectationRepository.findById(affectationId)
            .orElseThrow(() -> new RuntimeException("Affectation introuvable"));

    if (enseignantId != null) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));
        affectation.setEnseignant(enseignant);
    }

    if (matiereIds != null && !matiereIds.isEmpty()) {
        List<Matiere> matieres = matiereRepository.findAllById(matiereIds);

        if (matieres.isEmpty()) {
            throw new RuntimeException("Aucune matière trouvée");
        }

        affectation.setMatieres(matieres); // ✅ IMPORTANT
    }

    if (classeId != null) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));
        affectation.setClasse(classe);
    }

    affectation.setDateModification(LocalDate.now());

    return affectationRepository.save(affectation);
}

    // public AffectationDTO getAffectationDetailById(Long id) {
    //     return affectationRepository.findFullById(id)
    //             .orElseThrow(() -> new RuntimeException("Affectation non trouvée avec id : " + id));
    // }


    // 3️⃣ Supprimer une affectation
    public void supprimerAffectation(Long affectationId){
        affectationRepository.deleteById(affectationId);
    }

    // 4️⃣ Récupérer par enseignant
    public List<Affectation> getAffectationsParEnseignant(Long enseignantId){
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));
        return affectationRepository.findByEnseignant(enseignant);
    }

    // 5️⃣ Récupérer par classe
    public List<Affectation> getAffectationsParClasse(Long classeId){
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));
        return affectationRepository.findByClasse(classe);
    }

    public List<Affectation> getAll(){
        List<Affectation> affectations = affectationRepository.findAll();

        return affectations;
    }
    // 6️⃣ Récupérer par matière
    // public List<Affectation> getAffectationsParMatiere(Long matiereId){
    //     Matiere matiere = matiereRepository.findById(matiereId)
    //             .orElseThrow(() -> new RuntimeException("Matière introuvable"));
    //     return affectationRepository.findByMatiere(matiere);
    // }

    // 7️⃣ Récupérer par classe et année scolaire
    // public List<Affectation> getAffectationsParClasseEtAnnee(Long classeId, Long anneeId){
    //     return affectationRepository.findByClasseEtAnnee(classeId, anneeId);
    // }

    // 8️⃣ Récupérer par ID
    public Affectation getAffectationById(Long affectationId){
        return affectationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable"));
    }
}