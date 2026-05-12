# Meteo - Application Météo Moderne

Meteo est une application Android moderne, rapide et fiable pour consulter les prévisions météorologiques. Elle met l'accent sur la robustesse et une interface utilisateur fluide développée avec Jetpack Compose.

## ✨ Fonctionnalités

- **Prévisions Complètes** : Visualisez la météo actuelle, les prévisions horaires (12 prochaines heures) et quotidiennes (15 jours).
- **Recherche de Villes** : Recherchez n'importe quelle ville dans le monde.
- **Gestion des Favoris** : Enregistrez vos lieux préférés pour un accès rapide.
- **Mode Hors Ligne** : Consultez les dernières données récupérées même sans connexion internet.
- **Robustesse (Multi-Sources)** : Architecture intelligente avec fallback automatique. Si la source principale est indisponible, l'application bascule automatiquement sur une source secondaire.

## 🛠 Tech Stack

- **Langage** : Kotlin
- **UI** : Jetpack Compose (Material 3)
- **Architecture** : Clean Architecture + MVVM
- **Réseau** : Retrofit & OkHttp
- **JSON Parsing** : GSON
- **Local Storage** : SharedPreferences (via KTX)
- **Asynchronisme** : Kotlin Coroutines & Flow

## 🏗 Architecture & Design Patterns

L'application utilise une architecture évolutive basée sur le pattern **Adapter** pour la gestion des données météo :

- **WeatherProvider Interface** : Définit un contrat unique pour toutes les sources de données.
- **Multi-API Support** :
    - **Open-Meteo** (Source principale)
    - **MET Norway** (Fallback automatique)
- **Repository Pattern** : Centralise la logique de récupération des données et gère la priorité entre les API et le cache local.

## 🚀 Installation

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/votre-username/Meteo.git
   ```
2. Ouvrez le projet dans **Android Studio Ladybug (ou plus récent)**.
3. Synchronisez le projet avec les fichiers Gradle.
4. Lancez l'application sur un émulateur ou un appareil physique.

## 📝 Licence

Ce projet est sous licence **GPL v3**. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

### Third party

Icons are from [Airycons](https://github.com/HaroleDev/Airycons).

---
*Développé avec passion pour une météo toujours accessible.*
