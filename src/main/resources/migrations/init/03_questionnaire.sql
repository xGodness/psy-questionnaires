CREATE TABLE IF NOT EXISTS questionnaire (
    id              bigint          PRIMARY KEY         GENERATED ALWAYS AS IDENTITY,
    name            text            UNIQUE NOT NULL     CHECK (name <> ''),
    display_name    text            NOT NULL            CHECK (display_name <> '')
);