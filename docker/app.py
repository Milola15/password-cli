from flask import Flask, request, jsonify
from zxcvbn import zxcvbn

# Serveur Flask minimal qui expose un endpoint POST /check
# Il reçoit un mot de passe en JSON et retourne le score zxcvbn (0 à 4)
app = Flask(__name__)

# Correspondance entre score zxcvbn (0-4) et nos labels Java (0-5)
# zxcvbn utilise une échelle 0-4, on la mappe sur nos 5 niveaux
LABELS = {
    0: "Très faible",
    1: "Faible",
    2: "Moyen",
    3: "Fort",
    4: "Très fort"
}

@app.route("/check", methods=["POST"])
def check_password():
    """
    Reçoit { "password": "..." } en JSON
    Retourne { "score": 3, "label": "Fort", "feedback": "..." }
    """
    data = request.get_json()

    if not data or "password" not in data:
        return jsonify({"error": "Champ 'password' manquant"}), 400

    password = data["password"]

    # Analyse zxcvbn — retourne score, temps estimé de crack, suggestions
    result = zxcvbn(password)
    score  = result["score"]  # entier entre 0 et 4
    label  = LABELS[score]

    # On récupère les suggestions en français si disponibles
    feedback = result["feedback"]["suggestions"]
    feedback_text = " | ".join(feedback) if feedback else "Aucune suggestion"

    return jsonify({
        "score":    score,
        "label":    label,
        "feedback": feedback_text,
        "crack_time": result["crack_times_display"]["offline_slow_hashing_1e4_per_second"]
    })

@app.route("/health", methods=["GET"])
def health():
    """ Endpoint de vérification — permet à Java de tester si le serveur est actif """
    return jsonify({"status": "ok"})

if __name__ == "__main__":
    # 0.0.0.0 pour être accessible depuis l'extérieur du conteneur
    app.run(host="0.0.0.0", port=5000)