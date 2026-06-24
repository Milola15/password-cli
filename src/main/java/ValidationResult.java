package org.example;

/**
 * Contient le résultat de la validation Docker.
 *
 * On encapsule les données dans un objet plutôt que de retourner
 * plusieurs valeurs séparées — c'est plus propre et extensible.
 *
 * @author Milola
 * @version 1.0
 */
public class ValidationResult {

    private final boolean success;   // false si Docker est inaccessible
    private final int score;         // score zxcvbn (0-4)
    private final String label;      // label lisible (ex: "Fort")
    private final String crackTime;  // temps estimé pour craquer

    public ValidationResult(boolean success, int score, String label, String crackTime) {
        this.success   = success;
        this.score     = score;
        this.label     = label;
        this.crackTime = crackTime;
    }

    public boolean isSuccess()    { return success; }
    public int getScore()         { return score; }
    public String getLabel()      { return label; }
    public String getCrackTime()  { return crackTime; }
}