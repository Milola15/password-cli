package org.example;

/**
 * Évaluateur de robustesse d'un mot de passe.
 *
 * Le score est calculé localement selon plusieurs critères cumulatifs :
 * longueur, présence de majuscules, minuscules, chiffres et symboles.
 * Ce score local sera ensuite confirmé par l'outil externe dans Docker.
 *
 * @author Milola
 * @version 1.0
 */
public class StrengthEvaluator {

    // Seuils de longueur qui influencent le score
    private static final int LENGTH_MEDIUM = 8;
    private static final int LENGTH_STRONG = 12;
    private static final int LENGTH_VERY_STRONG = 16;

    /**
     * Calcule un score de robustesse entre 0 et 5.
     *
     * Chaque critère rapporte 1 point. La longueur peut rapporter
     * jusqu'à 2 points selon le seuil atteint — c'est le critère
     * le plus important en sécurité réelle.
     *
     * @param password Le mot de passe à évaluer
     * @return Un score entier entre 0 (très faible) et 5 (très fort)
     */
    public int calculateScore(String password) {
        int score = 0;

        // Critère 1 : longueur (jusqu'à 2 points)
        // Plus un mot de passe est long, plus il résiste aux attaques brute-force
        if (password.length() >= LENGTH_VERY_STRONG) {
            score += 2; // longueur excellente
        } else if (password.length() >= LENGTH_STRONG) {
            score += 1; // longueur bonne
        } else if (password.length() < LENGTH_MEDIUM) {
            score += 0; // trop court, aucun point
        }

        // Critère 2 : présence de majuscules (+1 point)
        if (password.chars().anyMatch(Character::isUpperCase)) {
            score++;
        }

        // Critère 3 : présence de minuscules (+1 point)
        if (password.chars().anyMatch(Character::isLowerCase)) {
            score++;
        }

        // Critère 4 : présence de chiffres (+1 point)
        if (password.chars().anyMatch(Character::isDigit)) {
            score++;
        }

        // Critère 5 : présence de symboles spéciaux (+1 point)
        // On vérifie qu'au moins un caractère n'est ni lettre ni chiffre
        if (password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) {
            score++;
        }

        return score;
    }

    /**
     * Convertit le score numérique en label lisible pour l'utilisateur.
     *
     * @param score Le score calculé par calculateScore()
     * @return Une chaîne décrivant le niveau de sécurité
     */
    public String getLabel(int score) {
        if (score <= 1) return "Très faible";
        if (score == 2) return "Faible";
        if (score == 3) return "Moyen";
        if (score == 4) return "Fort";
        if (score == 5) return "Très fort";
        return "Inconnu";
    }
}
