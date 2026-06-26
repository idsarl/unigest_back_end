package gestion.scolaire.service;

import gestion.scolaire.model.AppreciationSeuil;
import gestion.scolaire.model.ParametreEcole;
import gestion.scolaire.repository.AppreciationSeuilRepository;
import gestion.scolaire.repository.ParametreEcoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParametreEcoleService {

    private final ParametreEcoleRepository parametreEcoleRepository;
    private final AppreciationSeuilRepository appreciationSeuilRepository;

    // ── Paramètres école ──────────────────────────────────────────────────────

    /** Retourne le paramètre unique, ou en crée un par défaut. */
    @Transactional(readOnly = true)
    public ParametreEcole getParametres() {
        return parametreEcoleRepository.findAll()
                .stream().findFirst()
                .orElseGet(this::creerParametresDefaut);
    }

    public ParametreEcole sauvegarderParametres(ParametreEcole dto) {
        ParametreEcole existant = parametreEcoleRepository.findAll()
                .stream().findFirst().orElse(new ParametreEcole());

        existant.setNomEcole(dto.getNomEcole());
        existant.setAdresseEcole(dto.getAdresseEcole());
        existant.setTelephoneEcole(dto.getTelephoneEcole());
        existant.setQuotaClasse(dto.getQuotaClasse());
        existant.setQuotaComposition(dto.getQuotaComposition());
        existant.setCoefficientConduite(dto.getCoefficientConduite());

        return parametreEcoleRepository.save(existant);
    }

    private ParametreEcole creerParametresDefaut() {
        ParametreEcole p = new ParametreEcole();
        p.setNomEcole("Mon École");
        p.setQuotaClasse(40.0);
        p.setQuotaComposition(60.0);
        return parametreEcoleRepository.save(p);
    }

    // ── Seuils d'appréciation ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AppreciationSeuil> getSeuils() {
        List<AppreciationSeuil> seuils = appreciationSeuilRepository.findAllByOrderBySeuilMinDesc();
        if (seuils.isEmpty()) {
            return initialiserSeuilsDefaut();
        }
        return seuils;
    }

    public List<AppreciationSeuil> sauvegarderSeuils(List<AppreciationSeuil> seuils) {
        appreciationSeuilRepository.deleteAll();
        seuils.forEach(s -> s.setId(null));
        return appreciationSeuilRepository.saveAll(seuils);
    }

    public void supprimerSeuil(Long id) {
        appreciationSeuilRepository.deleteById(id);
    }

    /** Charge les seuils standard si la table est vide. */
    private List<AppreciationSeuil> initialiserSeuilsDefaut() {
        List<AppreciationSeuil> defauts = List.of(
            seuil(18.0, "Excellent"),
            seuil(16.0, "Très bien"),
            seuil(14.0, "Bien"),
            seuil(12.0, "Assez bien"),
            seuil(10.0, "Passable"),
            seuil(5.0,  "Insuffisant"),
            seuil(0.0,  "Très insuffisant")
        );
        return appreciationSeuilRepository.saveAll(defauts);
    }

    private AppreciationSeuil seuil(double min, String libelle) {
        AppreciationSeuil s = new AppreciationSeuil();
        s.setSeuilMin(min);
        s.setLibelle(libelle);
        return s;
    }

    /** Calcule l'appréciation à partir des seuils configurés. */
    public String calculerAppreciation(double moyenne) {
        return appreciationSeuilRepository.findAllByOrderBySeuilMinDesc()
                .stream()
                .filter(s -> moyenne >= s.getSeuilMin()
                          && (s.getSeuilMax() == null || moyenne <= s.getSeuilMax()))
                .findFirst()
                .map(AppreciationSeuil::getLibelle)
                .orElse("—");
    }
}
