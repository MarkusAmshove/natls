./gradlew clean check shadowJar && docker build -t sonarqube -f docker/Dockerfile.sonar  . && docker run --rm -p 9000:9000 sonarqube
