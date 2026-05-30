package gestion.scolaire.service;

import java.util.List;

import gestion.scolaire.model.EmploiDuTemps;

public interface EmploiDuTempsService {

    EmploiDuTemps save(EmploiDuTemps dto);

    EmploiDuTemps update(Long id, EmploiDuTemps dto);

    List<EmploiDuTemps> getAll();

    EmploiDuTemps getById(Long id);

    void delete(Long id);

    List<EmploiDuTemps> getByClasse(Long classeId);

    byte[] exportPdf(Long classeId);

    byte[] exportExcel(Long classeId);
    
}