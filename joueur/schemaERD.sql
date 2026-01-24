-- Ce script a été généré par l'outil ERD (Éditeur de diagramme entité-association) dans pgAdmin 4.
-- Si vous trouvez des bogues, veuillez les signaler sur https://github.com/pgadmin-org/pgadmin4/issues/new/choose. Merci de préciser les étapes de reproduction.
BEGIN;


CREATE TABLE IF NOT EXISTS public.ami
(
    joueur_pseudo character varying(50) COLLATE pg_catalog."default" NOT NULL,
    ami_pseudo character varying(50) COLLATE pg_catalog."default" NOT NULL,
    date_ajout timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    statut character varying(20) COLLATE pg_catalog."default" NOT NULL DEFAULT 'EN_ATTENTE'::character varying,
    CONSTRAINT ami_pkey PRIMARY KEY (joueur_pseudo, ami_pseudo)
);

CREATE TABLE IF NOT EXISTS public.editeur
(
    id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    nom character varying(255) COLLATE pg_catalog."default" NOT NULL,
    est_independant boolean NOT NULL DEFAULT false,
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT editeur_pkey PRIMARY KEY (id),
    CONSTRAINT editeur_nom_key UNIQUE (nom)
);

CREATE TABLE IF NOT EXISTS public.editeur_jeu
(
    editeur_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    jeu_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    date_publication timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT editeur_jeu_pkey PRIMARY KEY (editeur_id, jeu_id)
);

CREATE TABLE IF NOT EXISTS public.evaluation
(
    id serial NOT NULL,
    joueur_pseudo character varying(50) COLLATE pg_catalog."default" NOT NULL,
    jeu_id character varying(36) COLLATE pg_catalog."default",
    extension_id character varying(36) COLLATE pg_catalog."default",
    note integer NOT NULL,
    commentaire text COLLATE pg_catalog."default",
    date_publication timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    nombre_votes_utile integer NOT NULL DEFAULT 0,
    nombre_votes_pas_utile integer NOT NULL DEFAULT 0,
    CONSTRAINT evaluation_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.extension
(
    id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    titre character varying(255) COLLATE pg_catalog."default" NOT NULL,
    jeu_parent_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    prix numeric(10, 2) NOT NULL,
    version_jeu_base_requise character varying(20) COLLATE pg_catalog."default" NOT NULL,
    date_sortie timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT extension_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.jeu_catalogue
(
    id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    titre character varying(255) COLLATE pg_catalog."default" NOT NULL,
    editeur_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    plateforme character varying(50) COLLATE pg_catalog."default" NOT NULL,
    version_actuelle character varying(20) COLLATE pg_catalog."default" NOT NULL DEFAULT '1.0.0'::character varying,
    est_version_anticipee boolean NOT NULL DEFAULT false,
    prix_editeur numeric(10, 2) NOT NULL,
    prix_actuel numeric(10, 2) NOT NULL,
    date_publication timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT jeu_catalogue_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.jeu_genre
(
    jeu_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    genre character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT jeu_genre_pkey PRIMARY KEY (jeu_id, genre)
);

CREATE TABLE IF NOT EXISTS public.jeu_possede
(
    id serial NOT NULL,
    joueur_pseudo character varying(50) COLLATE pg_catalog."default" NOT NULL,
    jeu_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    version_installee character varying(20) COLLATE pg_catalog."default" NOT NULL,
    temps_jeu_minutes bigint NOT NULL DEFAULT 0,
    date_achat date NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT jeu_possede_pkey PRIMARY KEY (id),
    CONSTRAINT jeu_possede_joueur_pseudo_jeu_id_key UNIQUE (joueur_pseudo, jeu_id)
);

CREATE TABLE IF NOT EXISTS public.jeu_possede_extension
(
    jeu_possede_id integer NOT NULL,
    extension_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    date_achat date NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT jeu_possede_extension_pkey PRIMARY KEY (jeu_possede_id, extension_id)
);

CREATE TABLE IF NOT EXISTS public.joueur
(
    pseudo character varying(50) COLLATE pg_catalog."default" NOT NULL,
    nom character varying(100) COLLATE pg_catalog."default" NOT NULL,
    prenom character varying(100) COLLATE pg_catalog."default" NOT NULL,
    date_naissance date NOT NULL,
    date_inscription date NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT joueur_pkey PRIMARY KEY (pseudo)
);

CREATE TABLE IF NOT EXISTS public.modification_patch
(
    id serial NOT NULL,
    patch_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    type_modification character varying(20) COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT modification_patch_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.patch
(
    id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    jeu_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    plateforme character varying(50) COLLATE pg_catalog."default" NOT NULL,
    nouvelle_version character varying(20) COLLATE pg_catalog."default" NOT NULL,
    version_ciblee character varying(20) COLLATE pg_catalog."default" NOT NULL,
    commentaire_editeur text COLLATE pg_catalog."default",
    date_publication timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT patch_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.rapport_incident
(
    id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    jeu_id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    joueur_pseudo character varying(50) COLLATE pg_catalog."default" NOT NULL,
    version_jeu character varying(20) COLLATE pg_catalog."default" NOT NULL,
    plateforme character varying(50) COLLATE pg_catalog."default" NOT NULL,
    description_erreur text COLLATE pg_catalog."default" NOT NULL,
    date_survenue timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rapport_incident_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.votes_evaluation
(
    evaluation_id integer NOT NULL,
    votant_pseudo character varying(50) COLLATE pg_catalog."default" NOT NULL,
    est_utile boolean NOT NULL,
    date_vote timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT votes_evaluation_pkey PRIMARY KEY (evaluation_id, votant_pseudo)
);

ALTER TABLE IF EXISTS public.ami
    ADD CONSTRAINT ami_ami_pseudo_fkey FOREIGN KEY (ami_pseudo)
    REFERENCES public.joueur (pseudo) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.ami
    ADD CONSTRAINT ami_joueur_pseudo_fkey FOREIGN KEY (joueur_pseudo)
    REFERENCES public.joueur (pseudo) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.editeur_jeu
    ADD CONSTRAINT editeur_jeu_editeur_id_fkey FOREIGN KEY (editeur_id)
    REFERENCES public.editeur (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.editeur_jeu
    ADD CONSTRAINT editeur_jeu_jeu_id_fkey FOREIGN KEY (jeu_id)
    REFERENCES public.jeu_catalogue (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.evaluation
    ADD CONSTRAINT evaluation_extension_id_fkey FOREIGN KEY (extension_id)
    REFERENCES public.extension (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.evaluation
    ADD CONSTRAINT evaluation_jeu_id_fkey FOREIGN KEY (jeu_id)
    REFERENCES public.jeu_catalogue (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_evaluation_jeu
    ON public.evaluation(jeu_id);


ALTER TABLE IF EXISTS public.evaluation
    ADD CONSTRAINT evaluation_joueur_pseudo_fkey FOREIGN KEY (joueur_pseudo)
    REFERENCES public.joueur (pseudo) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_evaluation_joueur
    ON public.evaluation(joueur_pseudo);


ALTER TABLE IF EXISTS public.extension
    ADD CONSTRAINT extension_jeu_parent_id_fkey FOREIGN KEY (jeu_parent_id)
    REFERENCES public.jeu_catalogue (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.jeu_catalogue
    ADD CONSTRAINT jeu_catalogue_editeur_id_fkey FOREIGN KEY (editeur_id)
    REFERENCES public.editeur (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_jeu_editeur
    ON public.jeu_catalogue(editeur_id);


ALTER TABLE IF EXISTS public.jeu_genre
    ADD CONSTRAINT jeu_genre_jeu_id_fkey FOREIGN KEY (jeu_id)
    REFERENCES public.jeu_catalogue (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.jeu_possede
    ADD CONSTRAINT jeu_possede_jeu_id_fkey FOREIGN KEY (jeu_id)
    REFERENCES public.jeu_catalogue (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.jeu_possede
    ADD CONSTRAINT jeu_possede_joueur_pseudo_fkey FOREIGN KEY (joueur_pseudo)
    REFERENCES public.joueur (pseudo) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_jeu_possede_joueur
    ON public.jeu_possede(joueur_pseudo);


ALTER TABLE IF EXISTS public.jeu_possede_extension
    ADD CONSTRAINT jeu_possede_extension_extension_id_fkey FOREIGN KEY (extension_id)
    REFERENCES public.extension (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.jeu_possede_extension
    ADD CONSTRAINT jeu_possede_extension_jeu_possede_id_fkey FOREIGN KEY (jeu_possede_id)
    REFERENCES public.jeu_possede (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.modification_patch
    ADD CONSTRAINT modification_patch_patch_id_fkey FOREIGN KEY (patch_id)
    REFERENCES public.patch (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.patch
    ADD CONSTRAINT patch_jeu_id_fkey FOREIGN KEY (jeu_id)
    REFERENCES public.jeu_catalogue (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_patch_jeu
    ON public.patch(jeu_id);


ALTER TABLE IF EXISTS public.rapport_incident
    ADD CONSTRAINT rapport_incident_jeu_id_fkey FOREIGN KEY (jeu_id)
    REFERENCES public.jeu_catalogue (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_incident_jeu
    ON public.rapport_incident(jeu_id);


ALTER TABLE IF EXISTS public.rapport_incident
    ADD CONSTRAINT rapport_incident_joueur_pseudo_fkey FOREIGN KEY (joueur_pseudo)
    REFERENCES public.joueur (pseudo) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.votes_evaluation
    ADD CONSTRAINT votes_evaluation_evaluation_id_fkey FOREIGN KEY (evaluation_id)
    REFERENCES public.evaluation (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


ALTER TABLE IF EXISTS public.votes_evaluation
    ADD CONSTRAINT votes_evaluation_votant_pseudo_fkey FOREIGN KEY (votant_pseudo)
    REFERENCES public.joueur (pseudo) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;

END;