package gestion.scolaire.dto;

import java.time.LocalTime;

public class ProchaineCourseDTO {
    
    private Long seanceId;
    private String matiere;
    private String professeur;
    private String classe;
    private String filiere;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private long minutesRestantes;
    private long heuresRestantes;
    private String statut;
    
    // Constructeurs
    public ProchaineCourseDTO() {}
    
    public ProchaineCourseDTO(Long seanceId, String matiere, String professeur, 
                              String classe, String filiere, LocalTime heureDebut, 
                              LocalTime heureFin, long minutesRestantes, 
                              long heuresRestantes, String statut) {
        this.seanceId = seanceId;
        this.matiere = matiere;
        this.professeur = professeur;
        this.classe = classe;
        this.filiere = filiere;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.minutesRestantes = minutesRestantes;
        this.heuresRestantes = heuresRestantes;
        this.statut = statut;
    }
    
    // Getters et Setters
    public Long getSeanceId() {
        return seanceId;
    }
    
    public void setSeanceId(Long seanceId) {
        this.seanceId = seanceId;
    }
    
    public String getMatiere() {
        return matiere;
    }
    
    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }
    
    public String getProfesseur() {
        return professeur;
    }
    
    public void setProfesseur(String professeur) {
        this.professeur = professeur;
    }
    
    public String getClasse() {
        return classe;
    }
    
    public void setClasse(String classe) {
        this.classe = classe;
    }
    
    public String getFiliere() {
        return filiere;
    }
    
    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }
    
    public LocalTime getHeureDebut() {
        return heureDebut;
    }
    
    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }
    
    public LocalTime getHeureFin() {
        return heureFin;
    }
    
    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }
    
    public long getMinutesRestantes() {
        return minutesRestantes;
    }
    
    public void setMinutesRestantes(long minutesRestantes) {
        this.minutesRestantes = minutesRestantes;
    }
    
    public long getHeuresRestantes() {
        return heuresRestantes;
    }
    
    public void setHeuresRestantes(long heuresRestantes) {
        this.heuresRestantes = heuresRestantes;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
}
