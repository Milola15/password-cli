package org.example;

import java.util.List;
import java.util.Scanner;

/**
 * Interface en ligne de commande (CLI) de l'application.
 *
 * Cette classe gère toute l'interaction avec l'utilisateur :
 * elle pose les questions, collecte les réponses, puis orchestre
 * la génération et l'affichage des résultats.
 *
 * On affiche deux scores :
 * - Le score local (StrengthEvaluator) toujours disponible
 * - Le score Docker (zxcvbn) si le conteneur est actif
 *
 * @author Milola
 * @version 1.0
 */
public class CLI {

    private static final String SEPARATOR = "==================================================";

    private final Scanner           scanner   = new Scanner(System.in);
    private final StrengthEvaluator evaluator = new StrengthEvaluator();
    private final PasswordValidator validator = new PasswordValidator();

    // On vérifie Docker une seule fois au démarrage
    private final boolean dockerAvailable;

    public CLI() {
        this.dockerAvailable = validator.isDockerAvailable();
    }

    /**
     * Lance l'interface CLI — point d'entrée principal de l'interaction.
     */
    public void start() {
        printBanner();

        // Informer l'utilisateur si Docker est disponible ou non
        if (dockerAvailable) {
            System.out.println("  Docker : connecte (validation zxcvbn active)");
        } else {
            System.out.println("  Docker : non disponible (score local uniquement)");
        }
        System.out.println();

        // Collecter les préférences de l'utilisateur
        int length      = askLength();
        boolean upper   = askBoolean("Inclure des majuscules ? (o/n)");
        boolean lower   = askBoolean("Inclure des minuscules ? (o/n)");
        boolean digits  = askBoolean("Inclure des chiffres ?   (o/n)");
        boolean symbols = askBoolean("Inclure des symboles ?   (o/n)");
        int count       = askCount();

        // Sécurité : si aucun type sélectionné, forcer les minuscules
        if (!upper && !lower && !digits && !symbols) {
            System.out.println("\nAucun type selectionne — minuscules activees par defaut.");
            lower = true;
        }

        GeneratorPassword generator = new GeneratorPassword(length, upper, lower, digits, symbols);

        System.out.println("\n" + SEPARATOR);
        System.out.println("  RESULTATS");
        System.out.println(SEPARATOR);

        if (count == 1) {
            String pwd = generator.generate();
            printResult(1, pwd);
        } else {
            List<String> batch = generator.generateBatch(count);
            for (int i = 0; i < batch.size(); i++) {
                printResult(i + 1, batch.get(i));
            }
        }

        System.out.println(SEPARATOR);
        System.out.println("\nGeneration terminee !");

        if (askBoolean("\nGenerer d'autres mots de passe ? (o/n)")) {
            start();
        } else {
            System.out.println("Au revoir !");
            scanner.close();
        }
    }

    /**
     * Affiche le bandeau de bienvenue.
     */
    private void printBanner() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("   PASSWORD CLI - Pigier Cote d'Ivoire");
        System.out.println("   Generateur de mots de passe securises");
        System.out.println(SEPARATOR);
    }

    /**
     * Affiche un mot de passe avec son score local ET son score Docker.
     *
     * Si Docker est disponible, on affiche les deux scores pour montrer
     * que la validation externe confirme (ou non) le score local.
     *
     * @param index Numéro d'ordre
     * @param pwd   Le mot de passe à afficher
     */
    private void printResult(int index, String pwd) {

        // Score local — toujours calculé
        int localScore    = evaluator.calculateScore(pwd);
        String localLabel = evaluator.getLabel(localScore);

        System.out.println("\n  " + index + ". Mot de passe : " + pwd);
        System.out.println("     Score local  : " + localLabel);

        // Score Docker — seulement si le conteneur est actif
        if (dockerAvailable) {
            ValidationResult result = validator.validate(pwd);
            if (result.isSuccess()) {
                System.out.println("     Score Docker : " + result.getLabel()
                        + " (crack : " + result.getCrackTime() + ")");
            } else {
                System.out.println("     Score Docker : indisponible");
            }
        }
    }

    /**
     * Demande la longueur et valide entre 4 et 64.
     */
    private int askLength() {
        System.out.print("Longueur du mot de passe (4-64) : ");
        while (true) {
            try {
                int length = Integer.parseInt(scanner.nextLine().trim());
                if (length >= 4 && length <= 64) return length;
                System.out.print("Entrez un nombre entre 4 et 64 : ");
            } catch (NumberFormatException e) {
                System.out.print("Nombre invalide, reessayez : ");
            }
        }
    }

    /**
     * Demande le nombre de mots de passe et valide entre 1 et 50.
     */
    private int askCount() {
        System.out.print("Combien de mots de passe generer ? (1-50) : ");
        while (true) {
            try {
                int count = Integer.parseInt(scanner.nextLine().trim());
                if (count >= 1 && count <= 50) return count;
                System.out.print("Entrez un nombre entre 1 et 50 : ");
            } catch (NumberFormatException e) {
                System.out.print("Nombre invalide, reessayez : ");
            }
        }
    }

    /**
     * Pose une question oui/non.
     * Redemande tant que la réponse n'est ni "o" ni "n".
     */
    private boolean askBoolean(String question) {
        System.out.print(question + " ");
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("o")) return true;
            if (input.equals("n")) return false;
            System.out.print("Repondez par 'o' ou 'n' : ");
        }
    }
}