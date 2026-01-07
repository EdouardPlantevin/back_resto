package com.edouard.back_resto;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test d'intégration pour vérifier le chargement du contexte Spring.
 * Désactivé car nécessite une configuration complète de la base de données.
 * Pour l'activer, configurer une base de données de test (H2) et les propriétés nécessaires.
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Nécessite une configuration complète de la base de données")
class JwtApplicationTests {

	@Test
	void contextLoads() {
	}

}
