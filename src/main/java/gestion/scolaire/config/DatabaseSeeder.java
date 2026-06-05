package gestion.scolaire.config;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import gestion.scolaire.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Charge data_insert.sql une seule fois, si la base est vide ET si le fichier existe.
 * Évite les erreurs "Duplicate entry" au redémarrage.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (utilisateurRepository.count() > 0) {
            log.info("Base déjà initialisée ({} utilisateur(s)), skip data_insert.sql", utilisateurRepository.count());
            return;
        }

        Resource script = resourceLoader.getResource("classpath:data_insert.sql");
        if (!script.exists()) {
            log.info("Fichier data_insert.sql non trouvé, skip l'initialisation des données de test");
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(script);
        populator.setSeparator(";");
        populator.execute(dataSource);

        log.info("Données de test chargées depuis data_insert.sql");
    }
}
