# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
ARG MAVEN_OPTS=
ENV MAVEN_OPTS=${MAVEN_OPTS}

# 先拷贝 pom 以便缓存依赖
COPY pom.xml .
COPY timecampus-common/pom.xml timecampus-common/pom.xml
COPY timecampus-pojo/pom.xml timecampus-pojo/pom.xml
COPY timecampus-server/pom.xml timecampus-server/pom.xml

# 预下载依赖
RUN mvn -q -e -DskipTests dependency:go-offline

# 再拷贝源码并构建
COPY timecampus-common/src timecampus-common/src
COPY timecampus-pojo/src timecampus-pojo/src
COPY timecampus-server/src timecampus-server/src

RUN cp timecampus-server/src/main/resources/application-example.yaml timecampus-server/src/main/resources/application.yaml \
    && cp timecampus-server/src/main/resources/application-prod-example.yaml timecampus-server/src/main/resources/application-prod.yaml

RUN mvn -q -DskipTests package -pl timecampus-server -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/timecampus-server/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
