ENV_FILE=docker.env
DB_IMAGE_NAME=psy-questionnaires-db-1
APP_IMAGE_NAME=psy-questionnaires-app-1

build:
	mvn install -DskipTests

start: build
	docker compose --env-file $(ENV_FILE) up --build -d

stop:
	docker stop $(APP_IMAGE_NAME) $(DB_IMAGE_NAME)

drop:
	docker compose down