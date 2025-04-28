CREATE TABLE IF NOT EXISTS assignment (
    client_username     varchar(32)     REFERENCES app_user (username) ON DELETE CASCADE NOT NULL,
    questionnaire_id    bigint          REFERENCES questionnaire (id) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (client_username, questionnaire_id)
);