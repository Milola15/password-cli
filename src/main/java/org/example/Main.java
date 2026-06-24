
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
        System.out.println("=== Password CLI - Pigier Côte d'Ivoire ===\n");

        GeneratorPassword generator = new GeneratorPassword(12, true, true, true, true);
        StrengthEvaluator evaluator = new StrengthEvaluator();

        // Test 1 : un mot de passe avec son score
        String password = generator.generate();
        int score = evaluator.calculateScore(password);
        System.out.println("Mot de passe : " + password);
        System.out.println("Force       : " + evaluator.getLabel(score));

        // Test 2 : mode rafale avec score pour chacun
        System.out.println("\n--- Mode Rafale (5 mots de passe) ---");
        List<String> batch = generator.generateBatch(5);
        for (int i = 0; i < batch.size(); i++) {
            String pwd = batch.get(i);
            int s = evaluator.calculateScore(pwd);
            System.out.println((i + 1) + ". " + pwd + "  →  " + evaluator.getLabel(s));
        }

        // Test 3 : mot de passe faible (court, sans symboles)
        System.out.println("\n--- Test mot de passe faible ---");
        GeneratorPassword weak = new GeneratorPassword(5, false, true, false, false);
        String weakPwd = weak.generate();
        int weakScore = evaluator.calculateScore(weakPwd);
        System.out.println("Mot de passe : " + weakPwd);
        System.out.println("Force       : " + evaluator.getLabel(weakScore));
    }
}