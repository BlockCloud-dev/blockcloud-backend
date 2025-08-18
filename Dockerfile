# ---------- Build ----------
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle version.properties ./
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew clean bootJar -x test -x jacocoTestCoverageVerification -x jacocoTestReport --no-daemon

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Terraform 설치
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && wget https://releases.hashicorp.com/terraform/1.7.0/terraform_1.7.0_linux_amd64.zip \
    && unzip terraform_1.7.0_linux_amd64.zip \
    && mv terraform /usr/local/bin/ \
    && rm terraform_1.7.0_linux_amd64.zip \
    && apt-get remove -y wget unzip \
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
