FROM openjdk:19-jdk

ARG JAR_FILENAME="psy-questionnaires.jar"
ARG JAR_PATH="target/${JAR_FILENAME}"
ENV DEPLOY_CMD="java -jar ${JAR_FILENAME}"

WORKDIR /app

COPY ${JAR_PATH} .
COPY src/main/resources/ ./src/main/resources/

CMD ${DEPLOY_CMD}