-- Schéma de la base de données 'commentaires' pour AppEditeur
-- Fichier : AppEditeur/ressources/db_commentaires.sql
-- Utiliser avec SQLite (jdbc:sqlite:AppEditeur/ressources/commentaires.db)

PRAGMA foreign_keys = ON;

-- Table unique qui regroupe tous les commentaires / rapports reçus
CREATE TABLE IF NOT EXISTS commentaires (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id TEXT UNIQUE NOT NULL,
    event_type TEXT NOT NULL, -- 'EVALUATION' ou 'INCIDENT'
    ts INTEGER NOT NULL, -- timestamp epoch millis

    -- Champs communs au jeu
    jeu_id TEXT,
    titre_jeu TEXT,
    editeur_id TEXT,

    -- Champs liés aux évaluations
    pseudo_joueur TEXT,
    note INTEGER,
    commentaire TEXT,
    temps_de_jeu_minutes INTEGER,
    version_jeu_evaluee TEXT,
    date_publication INTEGER,
    recommande INTEGER, -- 0/1
    aspects_positifs TEXT, -- JSON array stocké en texte
    aspects_negatifs TEXT, -- JSON array stocké en texte

    -- Champs liés aux incidents
    incident_id TEXT,
    version_jeu TEXT,
    plateforme TEXT,
    type_incident TEXT,
    description_erreur TEXT,
    date_survenue INTEGER,
    contexte TEXT,

    -- Raw payload Avro (string) pour audit / debug
    raw_payload TEXT,

    created_at INTEGER DEFAULT (strftime('%s','now') * 1000)
);

-- Indexes utiles
CREATE INDEX IF NOT EXISTS idx_commentaires_event_type ON commentaires(event_type);
CREATE INDEX IF NOT EXISTS idx_commentaires_editeur ON commentaires(editeur_id);
CREATE INDEX IF NOT EXISTS idx_commentaires_jeu ON commentaires(jeu_id);


-- Remplissage initial (exemples fictifs)
INSERT INTO commentaires (event_id, event_type, ts, jeu_id, titre_jeu, note, commentaire)
VALUES ('test-001', 'EVALUATION', 1706137200, 'game-42', 'Super Jeu Video', 5, 'Vraiment top !');


-- Exemple d'utilisation :
-- sqlite3 commentaires.db < db_commentaires.sql

