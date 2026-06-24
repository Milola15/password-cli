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
 * On sépare la CLI de la logique métier (GeneratorPassword, StrengthEvaluator)
 * pour respecter le principe de responsabilité unique.
 *
 * @author Milola
 * @version 1.0
 */
public class CLI {

    // Séparateur visuel — String.repeat() non disponible dans cette config Java
    private static final String SEPARATOR = "==================================================";

    private final Scanner scanner = new Scanner(System.in);
    private final StrengthEvaluator evaluator = new StrengthEvaluator();

    /**
     * Lance l'interface CLI — point d'entrée principal de l'interaction.
     */
    public void start() {
        printBanner();

        // Collecter les préférences de l'utilisateur
        int length      = askLength();
        boolean upper   = askBoolean("Inclure des majuscules ? (o/n)");
        boolean lower   = askBoolean("Inclure des minuscules ? (o/n)");
        boolean digits  = askBoolean("Inclure des chiffres ?   (o/n)");
        boolean symbols = askBoolean("Inclure des symboles ?   (o/n)");
        int count       = askCount();

        // Sécurité : si aucun type sélectionné, forcer les minuscules par défaut
        if (!upper && !lower && !digits && !symbols) {
            System.out.println("\nAucun type selectionne — minuscules activees par defaut.");
            lower = true;
        }

        // Créer le générateur avec les paramètres choisis
        GeneratorPassword generator = new GeneratorPassword(length, upper, lower, digits, symbols);

        System.out.println("\n" + SEPARATOR);
        System.out.println("  RESULTATS");
        System.out.println(SEPARATOR);

        if (count == 1) {
            // Mode simple — un seul mot de passe
            String pwd = generator.generate();
            printResult(1, pwd);
        } else {
            // Mode rafale — plusieurs mots de passe d'un coup
            List<String> batch = generator.generateBatch(count);
            for (int i = 0; i < batch.size(); i++) {
                printResult(i + 1, batch.get(i));
            }
        }

        System.out.println(SEPARATOR);
        System.out.println("\nGeneration terminee !");

        // Proposer de recommencer sans relancer le programme
        if (askBoolean("\nGenerer d'autres mots de passe ? (o/n)")) {
            start();
        } else {
            System.out.println("Au revoir !");
            scanner.close();
        }
    }

    /**
     * Affiche le bandeau de bienvenue au démarrage.
     */
    private void printBanner() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("   PASSWORD CLI - Pigier Cote d'Ivoire");
        System.out.println("   Generateur de mots de passe securises");
        System.out.println(SEPARATOR + "\n");
    }

    /**
     * Affiche un mot de passe numéroté avec son score de robustesse.
     *
     * @param index Numéro d'ordre dans la liste
     * @param pwd   Le mot de passe à afficher
     */
    private void printResult(int index, String pwd) {
        int score      = evaluator.calculateScore(pwd);
        String label   = evaluator.getLabel(score);
        System.out.printf("  %d. %-20s  Force : %s%n", index, pwd, label);
    }

    /**
     * Demande la longueur souhaitée et valide que c'est entre 4 et 64.
     *
     * @return La longueur choisie par l'utilisateur
     */
    private int askLength() {
        System.out.print("Longueur du mot de passe (4-64) : ");
        while (true) {
            try {
                int length = Integer.parseInt(scanner.nextLine().trim());
                if (length >= 4 && length <= 64) return length;
                System.out.print("Entrez un nombre entre 4 et 64 : ");
            } catch (NumberFormatException e) {
                // L'utilisateur a tapé autre chose qu'un nombre entier
                System.out.print("Nombre invalide, reessayez : ");
            }
        }
    }

    /**
     * Demande combien de mots de passe générer et valide entre 1 et 50.
     *
     * @return Le nombre de mots de passe souhaité
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
     * Pose une question oui/non et retourne true pour "o", false pour "n".
     * Redemande tant que la réponse n'est ni "o" ni "n".
     *
     * @param question La question à afficher
     * @return true si "o", false si "n"
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