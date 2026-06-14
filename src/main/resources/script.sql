DROP DATABASE IF EXISTS centre_formations;
CREATE DATABASE centre_formations CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE centre_formations;

-- ============================================================
-- TABLES STRUCTURELLES
-- ============================================================

CREATE TABLE pole (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE utilisateur (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN', 'FORMATEUR', 'ETUDIANT') NOT NULL DEFAULT 'ETUDIANT',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE formation (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    pole_id       INT NOT NULL,
    titre         VARCHAR(200) NOT NULL,
    description   TEXT,
    duree_heures  INT NOT NULL,
    prix          DECIMAL(8,2) NOT NULL,
    CONSTRAINT fk_formation_pole FOREIGN KEY (pole_id) REFERENCES pole(id) ON DELETE RESTRICT
);

CREATE TABLE session (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    formation_id  INT NOT NULL,
    formateur_id  INT,
    date_debut    DATE NOT NULL,
    horaire       VARCHAR(100) NOT NULL,
    modalite      ENUM('PRESENTIEL', 'EN_LIGNE') NOT NULL, -- Correction : Passage en MAJUSCULES pour Java
    capacite_max  INT NOT NULL,
    CONSTRAINT fk_session_formation FOREIGN KEY (formation_id) REFERENCES formation(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_formateur FOREIGN KEY (formateur_id) REFERENCES utilisateur(id) ON DELETE RESTRICT
);

CREATE TABLE inscription (
    id                        INT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id               INT NOT NULL,
    session_id                INT NOT NULL,
    date_inscription          DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut                    VARCHAR(50) NOT NULL DEFAULT 'INSCRIT', -- Harmonie : MAJUSCULES strictes
    communication_structuree  VARCHAR(50) UNIQUE NOT NULL,
    paiement_signale          BOOLEAN NOT NULL DEFAULT FALSE,
    paiement_valide           BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uq_etudiant_session (etudiant_id, session_id),
    CONSTRAINT fk_inscription_etudiant FOREIGN KEY (etudiant_id) REFERENCES utilisateur(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inscription_session FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE
);

-- ======
-- Index
-- ======
CREATE INDEX idx_session_dates ON session(date_debut);

DELIMITER //

-- ============================================================
-- TRIGGERS (INTÉGRITÉ ET FLUX AUTOMATIQUES)
-- ============================================================

-- 1. Vérifier la capacité maximale de la salle/salon avant l'insertion
CREATE TRIGGER trg_check_capacite
BEFORE INSERT ON inscription
FOR EACH ROW
BEGIN
    DECLARE nb_inscrits INT;
    DECLARE cap_max INT;

    SELECT COUNT(*) INTO nb_inscrits
    FROM inscription
    WHERE session_id = NEW.session_id
      AND statut != 'ABANDONNE'; -- Correction : Majuscule

    SELECT capacite_max INTO cap_max
    FROM session
    WHERE id = NEW.session_id;

    IF nb_inscrits >= cap_max THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Capacité maximale de la session atteinte.';
    END IF;
END //

-- 2. Générer automatiquement une VRAIE communication structurée belge au format strict 3, 4, 5
CREATE TRIGGER trg_generate_communication
BEFORE INSERT ON inscription
FOR EACH ROW
BEGIN
    DECLARE base10 VARCHAR(10);
    DECLARE modulo INT;
    DECLARE extra3 VARCHAR(3);

    -- Génération de 3 chiffres résiduels (basés sur un micro-compteur aléatoire sécurisé)
    SET extra3 = LPAD(FLOOR(100 + (RAND() * 899)), 3, '0');

    -- Assemblage de la base de 10 chiffres requis (3 id_étudiant + 4 id_session + 3 extra)
    SET base10 = CONCAT(
        LPAD(NEW.etudiant_id, 3, '0'),
        LPAD(NEW.session_id, 4, '0'),
        extra3
    );

    -- Calcul du Modulo 97 réglementaire standardisé
    SET modulo = MOD(CAST(base10 AS UNSIGNED), 97);
    IF modulo = 0 THEN
        SET modulo = 97;
    END IF;

    -- Sortie au format strict 3, 4 et 5 chiffres (Le bloc 5 contient les 3 extra + les 2 du modulo de contrôle)
    SET NEW.communication_structuree = CONCAT('+++',
        LPAD(NEW.etudiant_id, 3, '0'), '/',
        LPAD(NEW.session_id, 4, '0'), '/',
        extra3, LPAD(modulo, 2, '0'),
    '+++');
END //

-- 3. Transition automatique d'état de cycle de vie lors de l'approbation comptable
CREATE TRIGGER trg_update_statut_paiement
AFTER UPDATE ON inscription
FOR EACH ROW
BEGIN
    IF NEW.paiement_valide = TRUE AND OLD.paiement_valide = FALSE THEN
        UPDATE inscription
        SET statut = 'EN_COURS' -- Correction : Changement automatique vers l'état actif attendu par Java
        WHERE id = NEW.id;
    END IF;
END //

-- ============================================================
-- PROCEDURES STOCKÉES
-- ============================================================

-- 1. Procédure métier d'inscription d'un usager
CREATE PROCEDURE inscrire_etudiant(
    IN p_etudiant_id INT,
    IN p_session_id INT,
    OUT p_iban VARCHAR(50),
    OUT p_montant DECIMAL(10,2),
    OUT p_communication VARCHAR(30)
)
BEGIN
    DECLARE v_formation_id INT;
    DECLARE v_prix DECIMAL(10,2);
    DECLARE v_inscription_id INT;

    SELECT s.formation_id, f.prix
    INTO v_formation_id, v_prix
    FROM session s
    JOIN formation f ON f.id = s.formation_id
    WHERE s.id = p_session_id;

    INSERT INTO inscription (
        etudiant_id,
        session_id,
        date_inscription,
        statut,
        paiement_signale,
        paiement_valide,
        communication_structuree -- Gérée par le trigger, mais requise à blanc pour l'init
    ) VALUES (
        p_etudiant_id,
        p_session_id,
        NOW(),
        'INSCRIT', -- Statut initial standard
        FALSE,
        FALSE,
        'PENDING_GEN'
    );

    SET v_inscription_id = LAST_INSERT_ID();

    SELECT communication_structuree
    INTO p_communication
    FROM inscription
    WHERE id = v_inscription_id;

    SET p_iban = 'BE68 5390 0754 7034';
    SET p_montant = v_prix;
END //

-- 2. Traitement d'approbation bancaire
CREATE PROCEDURE approuver_paiement(
    IN p_inscription_id INT,
    IN p_approuver BOOLEAN
)
BEGIN
    IF p_approuver = TRUE THEN
        UPDATE inscription
        SET paiement_valide = TRUE,
            statut = 'EN_COURS'
        WHERE id = p_inscription_id;
    ELSE
        UPDATE inscription
        SET paiement_valide = FALSE,
            statut = 'ABANDONNE'
        WHERE id = p_inscription_id;
    END IF;
END //

-- 3. Extraction dynamique du planning d'un cadre enseignant
CREATE PROCEDURE get_planning_formateur(
    IN p_formateur_id INT
)
BEGIN
    SELECT
        s.id AS session_id,
        f.titre AS formation,
        s.date_debut,
        s.horaire,
        s.modalite,
        s.capacite_max,
        COUNT(i.id) AS nb_inscrits
    FROM session s
    JOIN formation f ON f.id = s.formation_id
    LEFT JOIN inscription i ON i.session_id = s.id
        AND i.statut != 'ABANDONNE'
    WHERE s.formateur_id = p_formateur_id
      AND s.date_debut >= CURDATE()
    GROUP BY s.id, f.titre, s.date_debut, s.horaire, s.modalite, s.capacite_max
    ORDER BY s.date_debut ASC;
END //

-- 4. Journal d'historique personnel confidentiel de l'élève
CREATE PROCEDURE get_historique_etudiant(
    IN p_etudiant_id INT
)
BEGIN
    SELECT
        i.id AS inscription_id,
        f.titre AS formation,
        p.nom AS pole,
        s.date_debut,
        s.modalite,
        i.statut,
        i.paiement_signale,
        i.paiement_valide,
        i.communication_structuree,
        f.prix
    FROM inscription i
    JOIN session s ON s.id = i.session_id
    JOIN formation f ON f.id = s.formation_id
    JOIN pole p ON p.id = f.pole_id
    WHERE i.etudiant_id = p_etudiant_id
    ORDER BY s.date_debut DESC;
END //

DELIMITER ;

-- ============================================================
-- JEU DE DONNÉES DE TEST COHÉRENT (UPPERCASE)
-- ============================================================
INSERT INTO pole (nom, description) VALUES
('Informatique', 'Développement web, bases de données et cybersécurité'),
('Langues', 'Cours d''anglais, de néerlandais et de français langue étrangère'),
('Management', 'Gestion d''équipe, leadership et communication en entreprise');

INSERT INTO utilisateur (id, nom, prenom, email, mot_de_passe, role) VALUES
                                                                         (1, 'Admin', 'System', 'admin@centre.be', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN'),
                                                                         (2, 'Dupont', 'Jean', 'jean.dupont@formateur.be', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'FORMATEUR'),
                                                                         (3, 'Lefebvre', 'Marie', 'm.lefebvre@formateur.be', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'FORMATEUR'),
                                                                         (4, 'Peeters', 'Lucas', 'lucas.peeters@student.be', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ETUDIANT'),
                                                                         (5, 'Martin', 'Sophie', 'sophie.martin@student.be', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ETUDIANT'),
                                                                         (6, 'Dubois', 'Thomas', 't.dubois@student.be', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ETUDIANT');

INSERT INTO formation (pole_id, titre, description, duree_heures, prix) VALUES
(1, 'Java Web Servlet', 'Apprendre le développement Java avec Servlets et JDBC', 60, 450.00),
(1, 'SQL Avancé', 'Maîtriser les requêtes complexes et l''optimisation', 40, 300.00),
(2, 'Anglais Business', 'Communication professionnelle en milieu anglophone', 30, 250.00);

INSERT INTO session (formation_id, formateur_id, date_debut, horaire, modalite, capacite_max) VALUES
(1, 2, '2025-09-01', 'Lundi 18:00 - 21:00', 'EN_LIGNE', 15),
(2, 2, '2025-10-01', 'Mardi 09:00 - 16:00', 'PRESENTIEL', 10),
(3, 3, '2025-09-15', 'Mercredi 14:00 - 17:00', 'EN_LIGNE', 20);

INSERT INTO inscription (etudiant_id, session_id, statut, communication_structuree, paiement_signale, paiement_valide) VALUES
(4, 1, 'EN_COURS', '+++001/2025/00001+++', true, true),
(5, 2, 'INSCRIT', '+++002/2025/00002+++', false, false),
(6, 3, 'TERMINE', '+++003/2025/00003+++', true, true);