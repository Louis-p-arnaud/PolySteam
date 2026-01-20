# Application PolySteam - Plateforme

## 📋 Description

Application de gestion de la plateforme PolySteam avec menu interactif en mode texte.
Les données sont chargées automatiquement depuis la base de données PostgreSQL au démarrage.

## 🔧 Configuration

### 1. Base de données PostgreSQL

Assurez-vous que votre base de données PostgreSQL est configurée et accessible.

Les informations de connexion sont dans le fichier `.env` :
```env
DB_HOST=86.252.172.215
DB_PORT=5432
DB_NAME=polysteam
DB_USER=polysteam_user
DB_PASSWORD=PolySteam2026!
```

### 2. Initialisation de la base de données

Exécutez le script SQL pour créer la structure et insérer les données de test :
```bash
psql -h 86.252.172.215 -U polysteam_user -d polysteam -f src/main/resources/database_polysteam.sql
```

## 🚀 Lancement

### Compilation
```bash
javac -d target/classes -cp "lib/*" src/main/java/**/*.java
```

### Exécution
```bash
java -cp "target/classes:lib/*" main
```

Ou avec Maven :
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="main"
```

## 📚 Fonctionnalités

### Menu Principal

1. **📚 Catalogue de jeux**
   - Afficher tous les jeux
   - Détails d'un jeu
   - Évaluations d'un jeu
   - Modifier le prix d'un jeu

2. **👥 Gestion des joueurs**
   - Afficher tous les joueurs
   - Détails d'un joueur
   - Bibliothèque d'un joueur
   - Amis d'un joueur
   - Ajouter un nouveau joueur

3. **🏢 Gestion des éditeurs**
   - Afficher tous les éditeurs
   - Jeux d'un éditeur

4. **🐛 Rapports d'incidents**
   - Afficher tous les incidents
   - Incidents d'un jeu spécifique
   - Signaler un nouvel incident

5. **📊 Statistiques de la plateforme**
   - Nombre d'éditeurs, jeux, joueurs, incidents
   - Jeu le mieux noté

6. **🔍 Rechercher un jeu**
   - Recherche par titre (partiel)

## 🗂️ Structure du projet

```
plateforme/
├── src/main/java/
│   ├── main.java                    # Point d'entrée de l'application
│   ├── config/
│   │   └── DatabaseConfig.java      # Configuration de la connexion BDD
│   ├── dao/                          # Data Access Objects
│   │   ├── EditeurDAO.java
│   │   ├── JeuCatalogueDAO.java
│   │   ├── JoueurDAO.java
│   │   ├── EvaluationDAO.java
│   │   └── RapportIncidentDAO.java
│   ├── model/                        # Classes métier
│   │   ├── Plateforme.java
│   │   ├── Editeur.java
│   │   ├── JeuCatalogue.java
│   │   ├── Joueur.java
│   │   ├── JeuPossede.java
│   │   ├── Evaluation.java
│   │   ├── Extension.java
│   │   ├── Patch.java
│   │   └── RapportIncident.java
│   ├── service/                      # Services métier
│   │   ├── CatalogueService.java
│   │   └── PricingService.java
│   └── kafka/                        # Gestion Kafka (à venir)
│       ├── EditeurEventConsumer.java
│       └── IncidentEventProducer.java
├── src/main/resources/
│   ├── database_polysteam.sql       # Script de création de la BDD
│   ├── consignes.md                 # Consignes du projet
│   └── avro/                        # Schémas Avro
│       ├── EvaluationEvent.avsc
│       └── RapportIncidentEvent.avsc
├── .env                             # Configuration (ne pas commiter!)
├── .gitignore
└── pom.xml
```

## 🔐 Sécurité

- Le fichier `.env` contient les informations sensibles de connexion
- **NE JAMAIS COMMITER** le fichier `.env` dans Git
- Le `.gitignore` est configuré pour l'exclure automatiquement

## 📝 Notes

### Données de test

Le script SQL contient déjà des données de test :
- 4 éditeurs (Nintendo, EA, Supergiant Games, ConcernedApe)
- 4 jeux (Zelda BOTW, FIFA 24, Hades, Stardew Valley)
- 4 joueurs avec leurs évaluations et amis
- 3 rapports d'incidents

### Prochaines étapes

- [ ] Intégration Kafka pour la communication événementielle
- [ ] Producer Kafka pour les rapports d'incidents
- [ ] Consumer Kafka pour les événements des éditeurs
- [ ] Schema Registry pour la gestion des schémas Avro
- [ ] Système de prix dynamique basé sur les évaluations

## 🆘 Dépannage

### Erreur de connexion à la base de données
- Vérifiez que PostgreSQL est démarré
- Vérifiez les credentials dans `.env`
- Vérifiez que le port 5432 est accessible

### Erreur "Driver PostgreSQL non trouvé"
- Vérifiez que le fichier `postgresql-42.7.1.jar` est dans le dossier `lib/`
- Ajoutez la dépendance Maven si nécessaire

### Les données ne se chargent pas
- Vérifiez que le script SQL a bien été exécuté
- Vérifiez les logs de la console pour voir les erreurs SQL

