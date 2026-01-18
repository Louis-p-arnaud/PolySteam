# [cite_start]Projet de groupe JVM2 / Ingénierie des données [cite: 1]

[cite_start]**Sujet :** Flux d'événements de jeux vidéos [cite: 2]
[cite_start]**Version :** v0 du 08.12.2025 [cite: 3]

---

## [cite_start]Scénario du projet [cite: 4]

### [cite_start]Gestion centralisée de jeux vidéos [cite: 5]
[cite_start]Une plateforme permet à ses utilisateurs inscrits de posséder des jeux, de les installer, d'y jouer, de les mettre à jour, de les évaluer, de les recommander, etc.[cite: 6].
* [cite_start]Des éditeurs de jeux vidéos publient des jeux, les améliorent (via des patchs), y ajoutent des contenus additionnels (DLCs)[cite: 7].
* [cite_start]Le jeu sur la plateforme produit des données d'utilisation, qui permettent notamment de produire des rapports d'incidents (aka plantages / crashs)[cite: 8].
* [cite_start]Ces rapports sont utilisés par les éditeurs pour proposer des correctifs (patchs)[cite: 9].

### [cite_start]Intérêts des parties prenantes [cite: 10]

[cite_start]**Les éditeurs veulent :** [cite: 11]
* [cite_start]Publier des jeux de qualité pour maximiser leurs ventes[cite: 12].
* [cite_start]Obtenir en temps réel des retours sur le fonctionnement de leur application et les exploiter pour fournir de meilleures versions de leurs jeux via des correctifs[cite: 13].

[cite_start]**Les plateformes de jeux veulent :** [cite: 14]
* [cite_start]Proposer la meilleure expérience (catalogue, fiabilité) à leurs utilisateurs et maximiser leurs ventes[cite: 15].
* [cite_start]Optimiser la mise en avant et les prix de vente de leurs jeux en fonction de l'expérience de jeu ressentie et des évaluations des joueurs[cite: 16].

[cite_start]**Les joueurs veulent :** [cite: 17]
* [cite_start]Avoir une expérience de jeu la plus agréable possible et l'accès à un grand nombre de jeux de qualité[cite: 18].
* [cite_start]Bénéficier des évaluations par d'autres joueurs[cite: 19].
* [cite_start]Disposer de moyens de favoriser la correction de leurs jeux et leurs extensions[cite: 20].

---

## [cite_start]Contraintes d'architecture [cite: 21]

> [cite_start]**Important** [cite: 22]
> [cite_start]* La communication entre les parties prenantes est distribuée et asynchrone[cite: 23].
> [cite_start]* Des événements doivent être publiés, et on doit avoir la garantie qu'ils seront traités par les parties prenantes concernées (*at-least-once delivery*)[cite: 24].
> * La communication doit être régie par des contrats sur les données échangées, et ces contrats peuvent être amenés à évoluer dans le temps pour permettre l'évolution des applications[cite: 25].
    > [cite_start]* Les applications doivent être indépendantes et communiquer au travers d'un flux d'événements[cite: 26].

---

## [cite_start]Éléments du cahier des charges [cite: 27]

[cite_start]Des applications indépendantes qui communiquent entre elles au travers d'un système de flux d'événements doivent permettre un certain nombre d'actions pour les parties prenantes : éditeurs, plateformes et joueurs [cite: 28-31].

### [cite_start]1. Éditeurs de jeux vidéo [cite: 32]
* [cite_start]Peuvent être des entreprises ou des indépendants[cite: 33].
* [cite_start]Publient des jeux vidéos[cite: 34].
* [cite_start]Sont (au moins) caractérisés par : un nom, une plateforme d'exécution (ex. PC, PS5), une liste de genres (ex. simulation, stratégie, etc.)[cite: 35].
* [cite_start]Ont un numéro de dernière version, et peuvent être en version anticipée (versions < 1.0)[cite: 36].
* [cite_start]Suivent les commentaires et rapports d'incidents concernant leurs jeux[cite: 37]:
    * [cite_start]Collection de l'ensemble des commentaires dans une base de données privée[cite: 38].
    * [cite_start]Routage des commentaires problématiques (portant par ex. sur la stabilité) et des rapports d'incidents pour traitements spécifiques[cite: 39].
* [cite_start]Publient des correctifs (patches)[cite: 40]:
    * [cite_start]S'appliquent à un jeu vidéo pour un support donné et aux versions antérieures à une version ciblée (ex. 1.5.2)[cite: 41].
    * [cite_start]Sont caractérisés par : la nouvelle version, un commentaire de l'éditeur, une liste de modifications (au moins de types : correction, ajout, optimisation)[cite: 42].

### [cite_start]2. Plateformes de jeux vidéo [cite: 43]
* [cite_start]Permettent l'inscription d'utilisateurs[cite: 44].
* [cite_start]S'abonnent aux événements des éditeurs[cite: 45].
* [cite_start]**Proposent des pages de descriptions :** [cite: 46]
    * [cite_start]D'éditeurs (au moins : listes de jeux)[cite: 47].
    * [cite_start]De jeux par support (au moins : titre, éditeur, version, historique des correctifs, évaluations par les joueurs) (en option : gestion du crossplay)[cite: 48, 49].
    * [cite_start]De joueurs (au moins : pseudo, date d'inscription, liste de jeux, évaluations de jeux, temps de jeu par jeu), avec un affichage adapté (ex. page de l'utilisateur, page d'un utilisateur ami, page d'un utilisateur autre)[cite: 50].
* [cite_start]**Permettent d'acquérir des jeux :** [cite: 51]
    * [cite_start]Fixent les prix en fonction de plusieurs critères (initialement : prix éditeur, puis variations en fonction de la qualité perçue du jeu et de sa demande)[cite: 52].
    * [cite_start]Mise en vente d'extensions (DLC) (nécessitent une version minimale d'un jeu pour le même support)[cite: 53].
* [cite_start]**Assurent un monitoring de l'exécution (distante) des jeux et de leur appréciation :** [cite: 54]
    * [cite_start]Des rapports d'incidents sont obtenus automatiquement et transférés aux éditeurs concernés[cite: 55].
    * [cite_start]Des évaluations par les utilisateurs sont obtenues auprès d'utilisateurs volontaires et transférées[cite: 56].

### [cite_start]3. Utilisateurs de plateformes (Joueurs) [cite: 57]
* [cite_start]S'inscrivent à une plateforme de jeux vidéo[cite: 58].
* [cite_start]Fournissent un certain nombre d'informations protégées par le RGPD (au moins : pseudo (unique), nom, prénom, date de naissance)[cite: 60, 61].
* [cite_start]Peuvent acquérir des (licences de) jeux pour un support[cite: 62].
* [cite_start]Peuvent consulter des pages de la plateforme (pages utilisateur, bibliothèque de jeux personnelle, jeux, éditeurs)[cite: 63, 64].
* [cite_start]**Ont un flux d'informations fondé sur leurs préférences et les jeux qu'ils possèdent ou souhaitent acquérir**, incluant par exemple[cite: 65]:
    * [cite_start]Des mises à jour ou extensions d'un jeu possédé[cite: 67].
    * [cite_start]De nouvelles évaluations ou des baisses de prix pour un jeu souhaité[cite: 68].
* [cite_start]Peuvent évaluer des jeux / extensions qu'ils possèdent et auxquels ils ont suffisamment joué[cite: 69, 70].
* [cite_start]Peuvent évaluer l'utilité d'une évaluation par un autre joueur (ex. utile / pas utile)[cite: 71].

---

## [cite_start]Contraintes techniques pour le projet [cite: 72]

### [cite_start]Langages et outils [cite: 73]
* [cite_start]**Langages de réalisation :** Java et Kotlin[cite: 74].
    * [cite_start]Utilisation de librairies tierces Java / Kotlin[cite: 75].
    * [cite_start]Développement d'applications en Java / Kotlin (sans GUI, mode texte possible)[cite: 76, 77].
* [cite_start]**Formats d'échanges de données :** [cite: 78]
    * [cite_start]Avro[cite: 79].
    * (Optionnellement) [cite_start]Protocol Buffers, JSON Schema[cite: 80].
* [cite_start]**Outils de gestion de flux d'événements :** [cite: 81]
    * [cite_start]Kafka (gestion des flux)[cite: 82].
    * [cite_start]Kafka Streams (traitements sur les flux)[cite: 83].
    * [cite_start]Schema Registry (gestion centralisée de schémas utilisable avec Kafka)[cite: 84].
    * (Optionnellement) [cite_start]Kafka Connect (connexion à des sources de données externes)[cite: 85].
* [cite_start]**Système de gestion de bases de données :** [cite: 86]
    * [cite_start]Choix libre (plutôt BDs relationnelles)[cite: 87].

### Architecture Technique (Schémas)
[cite_start]*(D'après les diagrammes des pages 6)* [cite: 88-118]
* **Flux :** Source -> Kafka Connector (Source) -> Kafka -> Kafka Connector (Sink) -> Data Target.
* **Gestion des schémas :** Les producteurs et consommateurs interagissent avec un **Schema Registry** pour sérialiser/désérialiser les données (Avro/Protobuf/JSON) via des IDs de schémas, avec un cache local.

---

## [cite_start]Données utilisées pour le projet [cite: 119]

* [cite_start]**Objectif :** Recourir à un dataset existant pour simuler la création de jeux par les éditeurs[cite: 122].
* [cite_start]Toutes sources de données possibles[cite: 123].
* [cite_start]Possibilité d'utiliser des librairies (ex. Datafaker)[cite: 124].
* [cite_start]Possibilité d'utiliser le jeu de données "VGSales" (Video Games Sales Data Analysis) au format CSV [cite: 125-127].

[cite_start]**Exemple de schéma CSV (VGSales) :** [cite: 128, 129]
`Name, Platform, Year, Genre, Publisher, NA_Sales, EU_Sales, JP_Sales, Other_Sales, Global_Sales`

[cite_start]**Exemple de données :** [cite: 130-139]
```csv
Captain Tsubasa: New Kick Off, DS, 2010, Sports, Konami Digital Entertainment,0,0.02,0.06,0,0.08
Puzzle Chronicles, PSP, 2010, Puzzle, Konami Digital Entertainment,0.07,0,0,0.01,0.08
Busou Shinki: Battle Masters, PSP, 2010, Action, Konami Digital Entertainment,0,0,0.07,0,0.07
pro evolution soccer 2011, PC, 2010, Sports, Konami Digital Entertainment,0,0.05,0,0.01,0.06
```