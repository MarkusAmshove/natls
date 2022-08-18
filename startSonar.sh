#!/usr/bin/env bash

./gradlew clean check shadowJar && java -jar libs/natlint/build/libs/natlint.jar export-rules &&  cp rules.xml libs/natqube/src/main/resources/ && docker build -t sonarqube -f docker/Dockerfile.sonar  . && docker run --rm -p 9000:9000 sonarqube
