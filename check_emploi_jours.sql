
-- Check all emploi du temps for enseignant
SELECT 
    edt.id,
    edt.enseignant_id,
    edt.classe_id,
    edt.matiere_id,
    edt.heure_debut,
    edt.heure_fin,
    edt.date_debut,
    edt.date_fin,
    edt.actif,
    edtj.jour,
    c.nom as classe_nom,
    m.nom as matiere_nom
FROM emploi_du_temps edt
LEFT JOIN emploi_du_temps_jours edtj ON edt.id = edtj.emploi_du_temps_id
LEFT JOIN classe c ON edt.classe_id = c.id
LEFT JOIN matiere m ON edt.matiere_id = m.id
ORDER BY edt.enseignant_id, edtj.jour, edt.heure_debut;

