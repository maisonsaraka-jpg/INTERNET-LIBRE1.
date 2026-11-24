# Internet libre — prototype Android v1.2

## Fonctionnalités
- Interface orange et blanche.
- Sélection Orange CI, MTN CI ou Moov CI.
- Bouton ILLIMITÉ avec test de disponibilité pendant 5 secondes.
- Surveillance des pertes et retours de connexion pendant que l'application est ouverte.
- Indicateur du type de réseau et du signal lorsque Android le fournit.
- Conseils d'optimisation.
- Tableau de bord PING / DOWNLOAD / UPLOAD.
- Historique local des 5 derniers tests (conservé pendant la session de l'application).

## Consommation de données
Un test de débit télécharge environ 3 Mo et envoie environ 0,5 Mo, en plus des échanges techniques. Il peut consommer davantage selon les redirections et les réponses du serveur. Utilisez-le de préférence avec un forfait adapté ou le Wi-Fi.

## Limites
Les mesures sont indicatives et dépendent du serveur Cloudflare, du téléphone, de la couverture et de la charge du réseau. Le choix Orange/MTN/Moov est une étiquette de suivi : Android ne permet pas à une application ordinaire de sélectionner librement un opérateur ou de basculer la SIM.
Le suivi des changements réseau fonctionne lorsque l'application est ouverte ; ce prototype ne garantit pas une surveillance permanente en arrière-plan.
L'application ne rend pas un forfait illimité et ne peut pas augmenter la puissance radio ni réparer le réseau de l'opérateur.

## Générer l'APK
Ouvrir le projet dans Android Studio, synchroniser Gradle, puis choisir Build > Build Bundle(s) / APK(s) > Build APK(s). JDK 17 recommandé.
