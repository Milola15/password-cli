package org.example;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Générateur de mots de passe sécurisés.
 *
 * On utilise SecureRandom au lieu de Random classique parce que
 * SecureRandom est cryptographiquement sûr — ses valeurs ne sont
 * pas prévisibles, ce qui est essentiel pour la sécurité des mots de passe.
 *
 * @author Milola
 * @version 1.0
 */

public class GeneratorPassword {

    // --- Les alphabets disponibles ---
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String SYMBOLS   = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    // SecureRandom est partagé (une seule instance suffit, c'est thread-safe)
    private final SecureRandom random = new SecureRandom();

    // --- Paramètres de génération ---
    private final int length;
    private final boolean useUppercase;
    private final boolean useLowercase;
    private final boolean useDigits;
    private final boolean useSymbols;

    /**
     * Constructeur — reçoit tous les paramètres choisis par l'utilisateur.
     *
     * @param length       Longueur souhaitée du mot de passe
     * @param useUppercase Inclure des majuscules ?
     * @param useLowercase Inclure des minuscules ?
     * @param useDigits    Inclure des chiffres ?
     * @param useSymbols   Inclure des symboles ?
     */

    public GeneratorPassword(int length, boolean useUppercase, boolean useLowercase,
                             boolean useDigits, boolean useSymbols) {
        this.length       = length;
        this.useUppercase = useUppercase;
        this.useLowercase = useLowercase;
        this.useDigits    = useDigits;
        this.useSymbols   = useSymbols;
    }

/**
 * Génère un seul mot de passe selon les paramètres du constructeur.
 *
 * Stratégie : on garantit d'abord au moins 1 caractère de chaque type
 * activé, puis on complète avec des caractères aléatoires de l'alphabet
 * combiné. On mélange tout à la fin pour éviter un pattern prévisible
 * (ex : toujours une majuscule en premier).
 *
 * @return Le mot de passe généré sous forme de String
 * @throws IllegalStateException si aucun type de caractère n'est sélectionné
 */

public String generate() {

    // Construire l'alphabet combiné selon les options choisies
    StringBuilder alphabet = new StringBuilder();
    if (useUppercase) alphabet.append(UPPERCASE);
    if (useLowercase) alphabet.append(LOWERCASE);
    if (useDigits)    alphabet.append(DIGITS);
    if (useSymbols)   alphabet.append(SYMBOLS);

    // Sécurité : on ne peut pas générer sans caractères disponibles
    if (alphabet.length() == 0) {
        throw new IllegalStateException(
                "Erreur : sélectionnez au moins un type de caractère."
        );
    }

    // Liste de caractères — on travaille avec une liste pour pouvoir mélanger
    List<Character> passwordChars = new ArrayList<>();

    // Garantir au moins 1 caractère de chaque type activé
    // Cela évite qu'un mot de passe "avec symboles" n'en contienne aucun par malchance
    if (useUppercase) passwordChars.add(randomChar(UPPERCASE));
    if (useLowercase) passwordChars.add(randomChar(LOWERCASE));
    if (useDigits)    passwordChars.add(randomChar(DIGITS));
    if (useSymbols)   passwordChars.add(randomChar(SYMBOLS));

    // Compléter jusqu'à la longueur demandée avec l'alphabet complet
    String fullAlphabet = alphabet.toString();
    while (passwordChars.size() < length) {
        passwordChars.add(randomChar(fullAlphabet));
    }

    // Mélanger pour que les caractères garantis ne soient pas toujours en début
    Collections.shuffle(passwordChars, random);

    // Convertir la liste en String
    StringBuilder password = new StringBuilder();
    for (char c : passwordChars) {
        password.append(c);
    }

    return password.toString();
}

    /**
     * Mode Rafale — génère plusieurs mots de passe d'un coup.
     *
     * @param count Nombre de mots de passe à générer
     * @return Une liste de mots de passe
     */
    public List<String> generateBatch(int count) {
        List<String> passwords = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            passwords.add(generate());
        }
        return passwords;
    }

    /**
     * Retourne un caractère aléatoire pris dans la chaîne source donnée.
     * Méthode utilitaire privée — pas besoin de l'exposer à l'extérieur.
     *
     * @param source La chaîne dans laquelle piocher
     * @return Un caractère aléatoire de cette chaîne
     */
    private char randomChar(String source) {
        return source.charAt(random.nextInt(source.length()));
    }
}
