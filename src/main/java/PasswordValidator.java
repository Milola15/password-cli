package org.example;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Validateur de mots de passe via le conteneur Docker.
 *
 * Cette classe envoie une requête HTTP POST au serveur Flask
 * tournant dans Docker (port 5000) et récupère le score zxcvbn.
 *
 * On utilise HttpURLConnection (bibliothèque standard Java)
 * sans dépendance externe — pas besoin d'ajouter de librairie au pom.xml.
 *
 * @author Milola
 * @version 1.0
 */
public class PasswordValidator {

    // URL du serveur Flask dans le conteneur Docker
    private static final String DOCKER_URL = "http://localhost:5000/check";

    // Timeout en millisecondes — on n'attend pas indéfiniment si Docker est éteint
    private static final int TIMEOUT_MS = 3000;

    /**
     * Envoie le mot de passe au conteneur Docker et retourne le résultat.
     *
     * @param password Le mot de passe à valider
     * @return Un objet ValidationResult avec le score et le label Docker
     */
    public ValidationResult validate(String password) {

        try {
            // Préparer la connexion HTTP vers le serveur Flask
            URL url = new URL(DOCKER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // Corps de la requête JSON — on construit manuellement sans librairie externe
            String jsonBody = "{\"password\": \"" + escapeJson(password) + "\"}";

            // Envoyer le JSON dans le corps de la requête
            OutputStream os = connection.getOutputStream();
            os.write(jsonBody.getBytes("UTF-8"));
            os.flush();
            os.close();

            // Lire la réponse du serveur
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                Scanner sc = new Scanner(connection.getInputStream(), "UTF-8");
                StringBuilder response = new StringBuilder();
                while (sc.hasNextLine()) {
                    response.append(sc.nextLine());
                }
                sc.close();

                // Parser manuellement le JSON reçu
                String json = response.toString();
                int score = extractInt(json, "score");

                // decodeUnicode necessaire car Flask encode les accents en unicode
                String label = decodeUnicode(extractString(json, "label"));
                String crackTime = decodeUnicode(extractString(json, "crack_time"));

                return new ValidationResult(true, score, label, crackTime);

            } else {
                // Le serveur a répondu mais avec une erreur
                return new ValidationResult(false, -1, "Erreur serveur", "");
            }

        } catch (Exception e) {
            // Docker est éteint ou inaccessible — on le signale proprement
            return new ValidationResult(false, -1, "Docker non disponible", "");
        }
    }

    /**
     * Vérifie si le serveur Docker est accessible avant de valider.
     * Utilise l'endpoint /health du serveur Flask.
     *
     * @return true si Docker répond, false sinon
     */
    public boolean isDockerAvailable() {
        try {
            URL url = new URL("http://localhost:5000/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrait un entier d'une chaîne JSON simple.
     * On cherche la clé avec et sans espace après les deux points.
     *
     * @param json La chaîne JSON complète
     * @param key  La clé dont on veut la valeur entière
     * @return La valeur entière trouvée, ou -1 si non trouvée
     */
    private int extractInt(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\":");
            if (idx == -1) return -1;

            // Avancer après le ":"
            int start = idx + key.length() + 3;

            // Sauter les espaces éventuels
            while (start < json.length() && json.charAt(start) == ' ') start++;

            // Lire jusqu'à la virgule ou l'accolade fermante
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;

            return Integer.parseInt(json.substring(start, end).trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Extrait une chaîne de caractères d'une chaîne JSON simple.
     * Gère les espaces après les deux points.
     *
     * @param json La chaîne JSON complète
     * @param key  La clé dont on veut la valeur String
     * @return La valeur String trouvée, ou "" si non trouvée
     */
    private String extractString(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\":");
            if (idx == -1) return "";

            // Avancer après le ":" et trouver le guillemet ouvrant
            int start = idx + key.length() + 3;
            while (start < json.length() && json.charAt(start) != '"') start++;
            start++; // sauter le guillemet ouvrant

            // Lire jusqu'au guillemet fermant
            int end = start;
            while (end < json.length() && json.charAt(end) != '"') end++;

            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Échappe les caractères spéciaux dans une chaîne pour l'inclure en JSON.
     * Évite les injections si le mot de passe contient des guillemets.
     *
     * @param value La chaîne à échapper
     * @return La chaîne sécurisée pour inclusion dans du JSON
     */
    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Detecter une sequence unicode : backslash suivi de u et 4 chiffres hex.
     * Detecter une sequence unicode : backslash suivi de u et 4 chiffres hex
     *
     * @param input La chaîne avec des séquences Unicode
     * @return La chaîne avec les vrais caractères accentués
     */
    private String decodeUnicode(String input) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char current = input.charAt(i);
            char next = (i + 1 < input.length()) ? input.charAt(i + 1) : 0;

            // Détecter une séquence backslash + u + 4 caractères hex
            if (current == '\\' && next == 'u' && i + 5 < input.length()) {
                String hex = input.substring(i + 2, i + 6);
                try {
                    result.append((char) Integer.parseInt(hex, 16));
                    i += 6;
                } catch (NumberFormatException e) {
                    // Ce n'est pas une séquence unicode valide, on garde tel quel
                    result.append(current);
                    i++;
                }
            } else {
                result.append(current);
                i++;
            }
        }
        return result.toString();
    }
}