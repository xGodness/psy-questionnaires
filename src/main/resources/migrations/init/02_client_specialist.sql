CREATE TABLE IF NOT EXISTS client_specialist (
    client_username         varchar(32)     REFERENCES app_user (username) ON DELETE CASCADE NOT NULL,
    specialist_username     varchar(32)     REFERENCES app_user (username) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (client_username, specialist_username)
);