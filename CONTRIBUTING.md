# Introduction

Thank you for considering contributing to `natls`!

# Development Guide

We're looking for all kinds of contributions in all the different subprojects, may it be additional statements that get parsed, new analyzers, language server endpoints, updates to the docs, filing issues or feature requests.

## GitHub Workflow

1. [Fork this repository](https://github.com/MarkusAmshove/natls) on GitHub.

2. Clone your fork locally:
    ```bash
    $ git clone <url-to-your-fork>
    ```

3. Add the official repository (`upstream`) as a remote repository:
    ```bash
    $ git remote add upstream https://github.com/MarkusAmshove/natls.git
    ```

4. Build the project once to download all dependencies (or import it in your IDE):

    ```bash
    $ ./gradlew build
    ```

   This will download gradle and a JDK if they aren't already on your machine and also download all dependencies of `natls`.
   Furthermore it'll build the project and execute all tests.

5. Create a branch for your bugfix or feature based off the `main` branch:

    ```bash
    $ git checkout -b <name-bugfix-or-feature> main
    ```

   You can also name the branch based on subprojects, e.g. `natparse/parse-read-by` etc.

6. If the changes you make take a few days, be sure to occasionally pull the latest changes from `upstream`, to ensure
   that your local branch is up-to-date:

    ```bash
    $ git pull upstream main
    ```

7. When your work is done, push your local branch to your fork:

    ```bash
    $ git push origin <name-of-bugfix-or-feature>
    ```

8. [Create a pull request](https://help.github.com/articles/creating-a-pull-request-from-a-fork/)
   on GitHub.

NOTE: When the build fails after your changes and you don't know why, don't hesitate to create a pull request anyway, we might be able to help you!

# Building the project

All projects of `natls` are built with [Gradle](http://gradle.org/)

The default `gradlew build` will do a complete build of all subprojects.

The default build task will also run all tests and do a check on the [coding style](#coding-style).

If you want to get information about code coverage, you can run `gradlew cover` to run all tests and get an aggregated report. The report will be locateld in `<repository-root>/build/reports/jacoco/cover/html/index.html`

## Coding Style

To adhere to the formatting of the project, you can find formatter profiles in the repository, namely `EclipseFormatter.xml` and `IntelliFormatter.xml`.
Additionally, the formatting is checked by [Spotless](https://github.com/diffplug/spotless) in the Gradle build.

# How-To implement X

- [Parsing new statements](/docs/parsing-statements.md)
- [Implementing analyzers](/docs/implementing-analyzers.md)
- [Implementing quickfixes](/docs/implementing-quickfixes.md)
