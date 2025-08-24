---
title: "SonarQube Integration"
weight: 4
---

`natqube` requires at least SonarQube 9.9 (which is/was a LTS release).

The plugin is currently not distributed on the SonarQube marketplace.

Starting from v0.10 you can grab the [natqube.jar from the Releases page](https://github.com/MarkusAmshove/natls/releases) and put it into the plugins folder as described in [the SonarQube documentation](https://docs.sonarqube.org/latest/setup-and-upgrade/install-a-plugin/#manually-installing-plugins).

Alternatively you can build the jar yourself (`gradlew fatJar`) and put the file from `libs/natqube/build/libs/natqube.jar` into the plugin folder.

With a running SonarQube instance, you can analyze your project by following these steps:

- Run natlint in CI mode through Docker or the jar file:
-- `java -jar natlint.jar --ci`
-- `docker run --rm -u $(id -u):$(id -g) -v $PWD:/work ghcr.io/markusamshove/natlint:latest --ci`
- Run the [SonarQube scanner](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner/) in your project directory
