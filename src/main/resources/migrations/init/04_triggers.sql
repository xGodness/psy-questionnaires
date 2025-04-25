CREATE OR REPLACE FUNCTION check_client_specialist_roles()
    RETURNS TRIGGER AS $$
    BEGIN
        IF
            (SELECT role FROM app_user WHERE username = NEW.client_username) = 'CLIENT'
            AND (SELECT role FROM app_user WHERE username = NEW.specialist_username) = 'SPECIALIST'
        THEN RETURN NEW;
        ELSE RAISE EXCEPTION 'Insufficient roles on client_specialist table insertion attempt';
        END IF;
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER check_client_specialist_roles_trigger
BEFORE INSERT OR UPDATE ON client_specialist
FOR EACH ROW
EXECUTE FUNCTION check_client_specialist_roles();