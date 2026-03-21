-- =============================================================================
-- seed.sql  —  Spring Boot Flow Demo  (flowdb / PostgreSQL)
--
-- Matches entity: com.logi.flow.entity.UserEntity
--   @SequenceGenerator(sequenceName = "users_id_seq", allocationSize = 1)
--   @UniqueConstraint(name = "uk_users_email", columnNames = "email")
-- =============================================================================
--
-- ┌─────────────────────────────────────────────────────────────────────────┐
-- │  pgAdmin QUICK START (fresh machine — no DB yet)                        │
-- ├─────────────────────────────────────────────────────────────────────────┤
-- │                                                                         │
-- │  STEP 1 ► Create the database                                           │
-- │    Option A — pgAdmin GUI:                                              │
-- │      Right-click "Databases" → Create → Database                        │
-- │      Name: flowdb   Owner: postgres  → Save                             │
-- │                                                                         │
-- │    Option B — pgAdmin Query Tool, connected to "postgres" DB:           │
-- │      Run:  CREATE DATABASE flowdb;                                      │
-- │                                                                         │
-- │  STEP 2 ► Open Query Tool connected to flowdb                           │
-- │    In the pgAdmin browser tree, expand: Servers → postgres → Databases  │
-- │    Right-click "flowdb" → Query Tool                                    │
-- │                                                                         │
-- │  STEP 3 ► Paste this entire file and click ▶ Execute (F5)               │
-- │    The script is idempotent — run it again to wipe and re-seed.         │
-- │                                                                         │
-- └─────────────────────────────────────────────────────────────────────────┘

-- -----------------------------------------------------------------------------
-- 1. Drop & recreate sequence
-- -----------------------------------------------------------------------------
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- -----------------------------------------------------------------------------
-- 2. Drop & recreate table
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id          BIGINT        NOT NULL DEFAULT nextval('users_id_seq'),
    name        VARCHAR(50)   NOT NULL,
    email       VARCHAR(255)  NOT NULL,
    department  VARCHAR(100),
    created_at  TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_users         PRIMARY KEY (id),
    CONSTRAINT uk_users_email   UNIQUE (email)
);

-- Sequence ownership — lets DROP TABLE also drop the sequence
ALTER SEQUENCE users_id_seq OWNED BY users.id;

-- -----------------------------------------------------------------------------
-- 3. Seed data  (12 rows, 6 departments)
--    Timestamps are fixed so API test assertions stay deterministic.
-- -----------------------------------------------------------------------------
INSERT INTO users (id, name, email, department, created_at, updated_at) VALUES

-- Engineering (Flow 11a / 11b demo target)
(1,  'Alice Smith',    'alice@example.com',    'Engineering', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(2,  'Bob Johnson',    'bob@example.com',      'Engineering', '2026-01-02 09:00:00', '2026-01-02 09:00:00'),
(3,  'Carol Williams', 'carol@example.com',    'Engineering', '2026-01-03 09:00:00', '2026-01-03 09:00:00'),

-- Marketing
(4,  'David Brown',    'david@example.com',    'Marketing',   '2026-01-04 09:00:00', '2026-01-04 09:00:00'),
(5,  'Eve Davis',      'eve@example.com',      'Marketing',   '2026-01-05 09:00:00', '2026-01-05 09:00:00'),

-- HR
(6,  'Frank Miller',   'frank@example.com',    'HR',          '2026-01-06 09:00:00', '2026-01-06 09:00:00'),
(7,  'Grace Wilson',   'grace@example.com',    'HR',          '2026-01-07 09:00:00', '2026-01-07 09:00:00'),

-- Finance
(8,  'Henry Moore',    'henry@example.com',    'Finance',     '2026-01-08 09:00:00', '2026-01-08 09:00:00'),
(9,  'Iris Taylor',    'iris@example.com',     'Finance',     '2026-01-09 09:00:00', '2026-01-09 09:00:00'),

-- DevOps
(10, 'Jack Anderson',  'jack@example.com',     'DevOps',      '2026-01-10 09:00:00', '2026-01-10 09:00:00'),
(11, 'Karen Thomas',   'karen@example.com',    'DevOps',      '2026-01-11 09:00:00', '2026-01-11 09:00:00'),

-- Product
(12, 'Leo Jackson',    'leo@example.com',      'Product',     '2026-01-12 09:00:00', '2026-01-12 09:00:00');

-- -----------------------------------------------------------------------------
-- 4. Advance the sequence past the seeded IDs so next INSERT gets id=13
-- -----------------------------------------------------------------------------
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- -----------------------------------------------------------------------------
-- 5. Verify
-- -----------------------------------------------------------------------------
SELECT
    COUNT(*)                                   AS total_rows,
    COUNT(DISTINCT department)                 AS departments,
    MIN(id)                                    AS min_id,
    MAX(id)                                    AS max_id,
    currval('users_id_seq')                    AS next_id_will_be
FROM users;

SELECT department, COUNT(*) AS count
FROM users
GROUP BY department
ORDER BY department;
