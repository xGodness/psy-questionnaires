DROP TRIGGER IF EXISTS check_client_specialist_roles_trigger ON client_specialist;

DROP FUNCTION IF EXISTS check_client_specialist_roles();

DROP TABLE IF EXISTS assignment CASCADE;

DROP TABLE IF EXISTS questionnaire CASCADE;

DROP TABLE IF EXISTS client_specialist CASCADE;

DROP TABLE IF EXISTS app_user CASCADE;

DROP TYPE IF EXISTS role_t CASCADE;