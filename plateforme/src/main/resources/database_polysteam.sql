-- =============================================
-- Base de données PolySteam - Commune aux 3 applications
-- (Plateforme, Editeur, Joueur)
-- =============================================

-- Suppression des tables existantes (pour pouvoir réexécuter le script)
DROP TABLE IF EXISTS votes_evaluation;
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS rapport_incident;
DROP TABLE IF EXISTS modification_patch;
DROP TABLE IF EXISTS patch;
DROP TABLE IF EXISTS jeu_possede_extension;
DROP TABLE IF EXISTS jeu_possede;
DROP TABLE IF EXISTS extension;
DROP TABLE IF EXISTS jeu_genre;
DROP TABLE IF EXISTS jeu_catalogue;
DROP TABLE IF EXISTS editeur_jeu;
DROP TABLE IF EXISTS editeur;
DROP TABLE IF EXISTS ami;
DROP TABLE IF EXISTS joueur;

-- =============================================
-- TABLE: EDITEUR
-- =============================================
CREATE TABLE editeur (
    id VARCHAR(36) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL UNIQUE,
    est_independant BOOLEAN NOT NULL DEFAULT false,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLE: JEU_CATALOGUE
-- =============================================
CREATE TABLE jeu_catalogue (
    id VARCHAR(36) PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    editeur_id VARCHAR(36) NOT NULL,
    plateforme VARCHAR(50) NOT NULL, -- PC, PS5, Xbox, Switch, etc.
    version_actuelle VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    est_version_anticipee BOOLEAN NOT NULL DEFAULT false,
    prix_editeur DECIMAL(10, 2) NOT NULL,
    prix_actuel DECIMAL(10, 2) NOT NULL,
    date_publication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (editeur_id) REFERENCES editeur(id) ON DELETE CASCADE,
    CONSTRAINT chk_prix CHECK (prix_editeur >= 0 AND prix_actuel >= 0)
);

-- =============================================
-- TABLE: JEU_GENRE (relation many-to-many)
-- =============================================
CREATE TABLE jeu_genre (
    jeu_id VARCHAR(36) NOT NULL,
    genre VARCHAR(50) NOT NULL, -- Action, RPG, Simulation, Stratégie, Sport, etc.
    PRIMARY KEY (jeu_id, genre),
    FOREIGN KEY (jeu_id) REFERENCES jeu_catalogue(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: EDITEUR_JEU (pour tracer la relation)
-- =============================================
CREATE TABLE editeur_jeu (
    editeur_id VARCHAR(36) NOT NULL,
    jeu_id VARCHAR(36) NOT NULL,
    date_publication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (editeur_id, jeu_id),
    FOREIGN KEY (editeur_id) REFERENCES editeur(id) ON DELETE CASCADE,
    FOREIGN KEY (jeu_id) REFERENCES jeu_catalogue(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: EXTENSION (DLC)
-- =============================================
CREATE TABLE extension (
    id VARCHAR(36) PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    jeu_parent_id VARCHAR(36) NOT NULL,
    prix DECIMAL(10, 2) NOT NULL,
    version_jeu_base_requise VARCHAR(20) NOT NULL,
    date_sortie TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jeu_parent_id) REFERENCES jeu_catalogue(id) ON DELETE CASCADE,
    CONSTRAINT chk_prix_extension CHECK (prix >= 0)
);

-- =============================================
-- TABLE: PATCH (Correctifs)
-- =============================================
CREATE TABLE patch (
    id VARCHAR(36) PRIMARY KEY,
    jeu_id VARCHAR(36) NOT NULL,
    plateforme VARCHAR(50) NOT NULL,
    nouvelle_version VARCHAR(20) NOT NULL,
    version_ciblee VARCHAR(20) NOT NULL, -- Version jusqu'à laquelle le patch s'applique
    commentaire_editeur TEXT,
    date_publication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jeu_id) REFERENCES jeu_catalogue(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: MODIFICATION_PATCH
-- =============================================
CREATE TABLE modification_patch (
    id SERIAL PRIMARY KEY,
    patch_id VARCHAR(36) NOT NULL,
    type_modification VARCHAR(20) NOT NULL, -- CORRECTION, AJOUT, OPTIMISATION
    description TEXT NOT NULL,
    FOREIGN KEY (patch_id) REFERENCES patch(id) ON DELETE CASCADE,
    CONSTRAINT chk_type_modif CHECK (type_modification IN ('CORRECTION', 'AJOUT', 'OPTIMISATION'))
);

-- =============================================
-- TABLE: JOUEUR
-- =============================================
CREATE TABLE joueur (
    pseudo VARCHAR(50) PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    date_inscription DATE NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT chk_age CHECK (date_naissance < CURRENT_DATE)
);

-- =============================================
-- TABLE: AMI (Système d'amis)
-- =============================================
CREATE TABLE ami (
    joueur_pseudo VARCHAR(50) NOT NULL,
    ami_pseudo VARCHAR(50) NOT NULL,
    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE', -- EN_ATTENTE, ACCEPTE
    PRIMARY KEY (joueur_pseudo, ami_pseudo),
    FOREIGN KEY (joueur_pseudo) REFERENCES joueur(pseudo) ON DELETE CASCADE,
    FOREIGN KEY (ami_pseudo) REFERENCES joueur(pseudo) ON DELETE CASCADE,
    CONSTRAINT chk_different_users CHECK (joueur_pseudo <> ami_pseudo),
    CONSTRAINT chk_statut_ami CHECK (statut IN ('EN_ATTENTE', 'ACCEPTE'))
);

-- =============================================
-- TABLE: JEU_POSSEDE (Bibliothèque du joueur)
-- =============================================
CREATE TABLE jeu_possede (
    id SERIAL PRIMARY KEY,
    joueur_pseudo VARCHAR(50) NOT NULL,
    jeu_id VARCHAR(36) NOT NULL,
    version_installee VARCHAR(20) NOT NULL,
    temps_jeu_minutes BIGINT NOT NULL DEFAULT 0,
    date_achat DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (joueur_pseudo) REFERENCES joueur(pseudo) ON DELETE CASCADE,
    FOREIGN KEY (jeu_id) REFERENCES jeu_catalogue(id) ON DELETE CASCADE,
    UNIQUE (joueur_pseudo, jeu_id),
    CONSTRAINT chk_temps_jeu CHECK (temps_jeu_minutes >= 0)
);

-- =============================================
-- TABLE: JEU_POSSEDE_EXTENSION
-- =============================================
CREATE TABLE jeu_possede_extension (
    jeu_possede_id INT NOT NULL,
    extension_id VARCHAR(36) NOT NULL,
    date_achat DATE NOT NULL DEFAULT CURRENT_DATE,
    PRIMARY KEY (jeu_possede_id, extension_id),
    FOREIGN KEY (jeu_possede_id) REFERENCES jeu_possede(id) ON DELETE CASCADE,
    FOREIGN KEY (extension_id) REFERENCES extension(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: EVALUATION
-- =============================================
CREATE TABLE evaluation (
    id SERIAL PRIMARY KEY,
    joueur_pseudo VARCHAR(50) NOT NULL,
    jeu_id VARCHAR(36),
    extension_id VARCHAR(36),
    note INT NOT NULL,
    commentaire TEXT,
    date_publication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_votes_utile INT NOT NULL DEFAULT 0,
    nombre_votes_pas_utile INT NOT NULL DEFAULT 0,
    FOREIGN KEY (joueur_pseudo) REFERENCES joueur(pseudo) ON DELETE CASCADE,
    FOREIGN KEY (jeu_id) REFERENCES jeu_catalogue(id) ON DELETE CASCADE,
    FOREIGN KEY (extension_id) REFERENCES extension(id) ON DELETE CASCADE,
    CONSTRAINT chk_evaluation_target CHECK (
        (jeu_id IS NOT NULL AND extension_id IS NULL) OR
        (jeu_id IS NULL AND extension_id IS NOT NULL)
    ),
    CONSTRAINT chk_note CHECK (note >= 0 AND note <= 10),
    CONSTRAINT chk_votes CHECK (nombre_votes_utile >= 0 AND nombre_votes_pas_utile >= 0)
);

-- =============================================
-- TABLE: VOTES_EVALUATION (Méta-évaluation)
-- =============================================
CREATE TABLE votes_evaluation (
    evaluation_id INT NOT NULL,
    votant_pseudo VARCHAR(50) NOT NULL,
    est_utile BOOLEAN NOT NULL,
    date_vote TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (evaluation_id, votant_pseudo),
    FOREIGN KEY (evaluation_id) REFERENCES evaluation(id) ON DELETE CASCADE,
    FOREIGN KEY (votant_pseudo) REFERENCES joueur(pseudo) ON DELETE CASCADE
);

-- =============================================
-- TABLE: RAPPORT_INCIDENT
-- =============================================
CREATE TABLE rapport_incident (
    id VARCHAR(36) PRIMARY KEY,
    jeu_id VARCHAR(36) NOT NULL,
    joueur_pseudo VARCHAR(50) NOT NULL,
    version_jeu VARCHAR(20) NOT NULL,
    plateforme VARCHAR(50) NOT NULL,
    description_erreur TEXT NOT NULL,
    date_survenue TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jeu_id) REFERENCES jeu_catalogue(id) ON DELETE CASCADE,
    FOREIGN KEY (joueur_pseudo) REFERENCES joueur(pseudo) ON DELETE CASCADE
);

-- =============================================
-- INDEX pour optimiser les requêtes fréquentes
-- =============================================
CREATE INDEX idx_jeu_editeur ON jeu_catalogue(editeur_id);
CREATE INDEX idx_jeu_plateforme ON jeu_catalogue(plateforme);
CREATE INDEX idx_jeu_titre ON jeu_catalogue(titre);
CREATE INDEX idx_evaluation_jeu ON evaluation(jeu_id);
CREATE INDEX idx_evaluation_joueur ON evaluation(joueur_pseudo);
CREATE INDEX idx_incident_jeu ON rapport_incident(jeu_id);
CREATE INDEX idx_patch_jeu ON patch(jeu_id);
CREATE INDEX idx_jeu_possede_joueur ON jeu_possede(joueur_pseudo);

-- =============================================
-- DONNÉES DE TEST
-- =============================================

-- Insertion de quelques éditeurs
INSERT INTO editeur (id, nom, est_independant) VALUES
('e1111111-1111-1111-1111-111111111111', 'Nintendo', false),
('e2222222-2222-2222-2222-222222222222', 'Electronic Arts', false),
('e3333333-3333-3333-3333-333333333333', 'Supergiant Games', true),
('e4444444-4444-4444-4444-444444444444', 'ConcernedApe', true);

-- Insertion de quelques jeux
INSERT INTO jeu_catalogue (id, titre, editeur_id, plateforme, version_actuelle, est_version_anticipee, prix_editeur, prix_actuel) VALUES
('j1111111-1111-1111-1111-111111111111', 'The Legend of Zelda: Breath of the Wild', 'e1111111-1111-1111-1111-111111111111', 'Switch', '1.6.0', false, 59.99, 59.99),
('j2222222-2222-2222-2222-222222222222', 'FIFA 24', 'e2222222-2222-2222-2222-222222222222', 'PS5', '1.2.3', false, 69.99, 54.99),
('j3333333-3333-3333-3333-333333333333', 'Hades', 'e3333333-3333-3333-3333-333333333333', 'PC', '1.0.38865', false, 24.99, 24.99),
('j4444444-4444-4444-4444-444444444444', 'Stardew Valley', 'e4444444-4444-4444-4444-444444444444', 'PC', '1.6.9', false, 14.99, 14.99);

-- Genres des jeux
INSERT INTO jeu_genre (jeu_id, genre) VALUES
('j1111111-1111-1111-1111-111111111111', 'Action'),
('j1111111-1111-1111-1111-111111111111', 'Aventure'),
('j2222222-2222-2222-2222-222222222222', 'Sport'),
('j3333333-3333-3333-3333-333333333333', 'Roguelike'),
('j3333333-3333-3333-3333-333333333333', 'Action'),
('j4444444-4444-4444-4444-444444444444', 'Simulation'),
('j4444444-4444-4444-4444-444444444444', 'RPG');

-- Relation éditeur-jeu
INSERT INTO editeur_jeu (editeur_id, jeu_id) VALUES
('e1111111-1111-1111-1111-111111111111', 'j1111111-1111-1111-1111-111111111111'),
('e2222222-2222-2222-2222-222222222222', 'j2222222-2222-2222-2222-222222222222'),
('e3333333-3333-3333-3333-333333333333', 'j3333333-3333-3333-3333-333333333333'),
('e4444444-4444-4444-4444-444444444444', 'j4444444-4444-4444-4444-444444444444');

-- Insertion d'extensions (DLC)
INSERT INTO extension (id, titre, jeu_parent_id, prix, version_jeu_base_requise) VALUES
('x1111111-1111-1111-1111-111111111111', 'The Master Trials', 'j1111111-1111-1111-1111-111111111111', 19.99, '1.3.0'),
('x2222222-2222-2222-2222-222222222222', 'The Champions Ballad', 'j1111111-1111-1111-1111-111111111111', 19.99, '1.3.0');

-- Insertion de patches
INSERT INTO patch (id, jeu_id, plateforme, nouvelle_version, version_ciblee, commentaire_editeur) VALUES
('p1111111-1111-1111-1111-111111111111', 'j2222222-2222-2222-2222-222222222222', 'PS5', '1.2.4', '1.2.3', 'Correction de bugs majeurs et amélioration des performances'),
('p2222222-2222-2222-2222-222222222222', 'j4444444-4444-4444-4444-444444444444', 'PC', '1.6.10', '1.6.9', 'Ajout de nouveaux contenus et corrections');

-- Modifications des patches
INSERT INTO modification_patch (patch_id, type_modification, description) VALUES
('p1111111-1111-1111-1111-111111111111', 'CORRECTION', 'Correction du bug de crash lors de l''accès au menu'),
('p1111111-1111-1111-1111-111111111111', 'OPTIMISATION', 'Amélioration du temps de chargement des matchs'),
('p2222222-2222-2222-2222-222222222222', 'AJOUT', 'Nouveau festival saisonnier'),
('p2222222-2222-2222-2222-222222222222', 'CORRECTION', 'Correction de bugs mineurs');

-- Insertion de joueurs
INSERT INTO joueur (pseudo, nom, prenom, date_naissance) VALUES
('GamerPro123', 'Dupont', 'Jean', '1995-03-15'),
('ZeldaFan99', 'Martin', 'Sophie', '1998-07-22'),
('FifaKing', 'Bernard', 'Luc', '2000-11-08'),
('IndieGamer', 'Petit', 'Marie', '1992-05-30');

-- Système d'amis
INSERT INTO ami (joueur_pseudo, ami_pseudo, statut) VALUES
('GamerPro123', 'ZeldaFan99', 'ACCEPTE'),
('ZeldaFan99', 'GamerPro123', 'ACCEPTE'),
('FifaKing', 'GamerPro123', 'EN_ATTENTE'),
('IndieGamer', 'ZeldaFan99', 'ACCEPTE');

-- Jeux possédés par les joueurs
INSERT INTO jeu_possede (joueur_pseudo, jeu_id, version_installee, temps_jeu_minutes, date_achat) VALUES
('GamerPro123', 'j3333333-3333-3333-3333-333333333333', '1.0.38865', 3450, '2024-01-15'),
('GamerPro123', 'j4444444-4444-4444-4444-444444444444', '1.6.9', 12000, '2023-06-20'),
('ZeldaFan99', 'j1111111-1111-1111-1111-111111111111', '1.6.0', 18000, '2023-03-10'),
('FifaKing', 'j2222222-2222-2222-2222-222222222222', '1.2.3', 5400, '2024-10-05'),
('IndieGamer', 'j3333333-3333-3333-3333-333333333333', '1.0.38865', 7200, '2024-02-28');

-- Extensions possédées
INSERT INTO jeu_possede_extension (jeu_possede_id, extension_id) VALUES
(3, 'x1111111-1111-1111-1111-111111111111'),
(3, 'x2222222-2222-2222-2222-222222222222');

-- Évaluations
INSERT INTO evaluation (joueur_pseudo, jeu_id, extension_id, note, commentaire, nombre_votes_utile, nombre_votes_pas_utile) VALUES
('GamerPro123', 'j3333333-3333-3333-3333-333333333333', NULL, 10, 'Jeu absolument incroyable ! Le gameplay est parfait et l''histoire captivante.', 45, 2),
('GamerPro123', 'j4444444-4444-4444-4444-444444444444', NULL, 9, 'Un jeu relaxant et addictif. Parfait pour se détendre après une longue journée.', 120, 5),
('ZeldaFan99', 'j1111111-1111-1111-1111-111111111111', NULL, 10, 'Chef-d''œuvre absolu. Meilleur jeu de la Switch sans hésitation.', 250, 8),
('FifaKing', 'j2222222-2222-2222-2222-222222222222', NULL, 6, 'Bon jeu mais trop de bugs au lancement. Beaucoup de plantages.', 78, 15),
('IndieGamer', 'j3333333-3333-3333-3333-333333333333', NULL, 9, 'Excellent roguelike avec une rejouabilité infinie.', 92, 3),
('ZeldaFan99', NULL, 'x1111111-1111-1111-1111-111111111111', 8, 'DLC intéressant avec des défis corsés.', 34, 6);

-- Votes sur les évaluations
INSERT INTO votes_evaluation (evaluation_id, votant_pseudo, est_utile) VALUES
(1, 'ZeldaFan99', true),
(1, 'IndieGamer', true),
(2, 'FifaKing', true),
(3, 'GamerPro123', true),
(3, 'IndieGamer', true),
(4, 'GamerPro123', true),
(4, 'ZeldaFan99', false);

-- Rapports d'incidents
INSERT INTO rapport_incident (id, jeu_id, joueur_pseudo, version_jeu, plateforme, description_erreur) VALUES
('i1111111-1111-1111-1111-111111111111', 'j2222222-2222-2222-2222-222222222222', 'FifaKing', '1.2.3', 'PS5', 'Crash lors de l''accès au menu de carrière. Code erreur: CE-108255-1'),
('i2222222-2222-2222-2222-222222222222', 'j2222222-2222-2222-2222-222222222222', 'FifaKing', '1.2.3', 'PS5', 'Gel de l''image pendant les matchs en ligne'),
('i3333333-3333-3333-3333-333333333333', 'j4444444-4444-4444-4444-444444444444', 'GamerPro123', '1.6.9', 'PC', 'Bug d''affichage lors du festival de printemps');

-- =============================================
-- VUES UTILES
-- =============================================

-- Vue: Jeux avec leur note moyenne
CREATE VIEW vue_jeux_avec_notes AS
SELECT
    j.id,
    j.titre,
    j.plateforme,
    j.prix_actuel,
    e.nom AS editeur,
    COALESCE(AVG(ev.note), 0) AS note_moyenne,
    COUNT(ev.id) AS nombre_evaluations
FROM jeu_catalogue j
LEFT JOIN editeur e ON j.editeur_id = e.id
LEFT JOIN evaluation ev ON j.id = ev.jeu_id
GROUP BY j.id, j.titre, j.plateforme, j.prix_actuel, e.nom;

-- Vue: Statistiques joueurs
CREATE VIEW vue_stats_joueurs AS
SELECT
    j.pseudo,
    COUNT(DISTINCT jp.jeu_id) AS nombre_jeux_possedes,
    SUM(jp.temps_jeu_minutes) AS temps_total_jeu_minutes,
    COUNT(DISTINCT e.id) AS nombre_evaluations
FROM joueur j
LEFT JOIN jeu_possede jp ON j.pseudo = jp.joueur_pseudo
LEFT JOIN evaluation e ON j.pseudo = e.joueur_pseudo
GROUP BY j.pseudo;

-- Vue: Incidents par jeu
CREATE VIEW vue_incidents_par_jeu AS
SELECT
    j.id AS jeu_id,
    j.titre,
    e.nom AS editeur,
    j.version_actuelle,
    COUNT(ri.id) AS nombre_incidents
FROM jeu_catalogue j
LEFT JOIN editeur e ON j.editeur_id = e.id
LEFT JOIN rapport_incident ri ON j.id = ri.jeu_id
GROUP BY j.id, j.titre, e.nom, j.version_actuelle;

-- =============================================
-- FIN DU SCRIPT
-- =============================================

