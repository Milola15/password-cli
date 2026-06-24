
package org.example;
import java.util.List;

/**
 * Point d'entrée principal de l'application Password CLI.
 * Ce programme permet de générer des mots de passe sécurisés
 * et d'évaluer leur robustesse via un conteneur Docker.
 *
 * @author Milola
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Password CLI - Pigier Côte d'Ivoire ===");
        System.out.println("Démarrage de l'application...");

        // ---étape 2 : génération de mots de passe ---

        // : mot de passe standard (toutes options activées, longueur 12)
        GeneratorPassword generator = new GeneratorPassword(12, true, true, true, true);
        String password = generator.generate();
        System.out.println("Mot de passe généré : " + password);

        // : mode rafale — 5 mots de passe d'un coup
        System.out.println("\n--- Mode Rafale (5 mots de passe) ---");
        List<String> batch = generator.generateBatch(5);
        for (int i = 0; i < batch.size(); i++) {
            System.out.println((i + 1) + ". " + batch.get(i));
        }

        // Test 3 : sans symboles, longueur 8
        System.out.println("\n--- Sans symboles, longueur 8 ---");
        GeneratorPassword simple = new GeneratorPassword(8, true, true, true, false);
        System.out.println("Mot de passe : " + simple.generate());
    }
}