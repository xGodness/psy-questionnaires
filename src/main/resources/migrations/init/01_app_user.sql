DO $$
    BEGIN CREATE TYPE app_role_t AS ENUM ('CLIENT', 'SPECIALIST');
    EXCEPTION WHEN duplicate_object
        THEN null;
END $$;

CREATE TABLE IF NOT EXISTS app_user (
    username    varchar(64)     PRIMARY KEY,
    role        app_role_t      NOT NULL,
    salt        varchar(128)    NOT NULL,
    passhash    TEXT            NOT NULL
);