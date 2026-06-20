
-- Voir les affectations et les matières associées
SELECT a.id, a.classe_id, a.enseignant_id, am.matiere_id, m.nom as matiere_nom 
FROM affectation a 
LEFT JOIN affectation_matiere am ON a.id = am.affectation_id 
LEFT JOIN matiere m ON am.matiere_id = m.id;
