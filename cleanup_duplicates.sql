
-- Supprimer les doublons de séances, en gardant celle avec l'ID le plus petit
DELETE s1 FROM seance s1
INNER JOIN seance s2 
WHERE s1.id > s2.id 
AND s1.affectation_id = s2.affectation_id 
AND s1.date = s2.date 
AND s1.matiere = s2.matiere 
AND s1.heure_debut = s2.heure_debut;

-- Vérifier le résultat
SELECT * FROM seance;
