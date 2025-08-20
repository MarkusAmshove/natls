# Natural Language Server (natls)

![NatLS Logo](assets/logo_128x128.png)

[![Continuous Integration](https://github.com/MarkusAmshove/natlint-manual/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/MarkusAmshove/natlint-manual/actions/workflows/gradle-build.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=MarkusAmshove_natls&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=MarkusAmshove_natls) [![Documentation](https://img.shields.io/badge/docs-latest-blue.svg)](https://nat-ls.github.io/) [!GitHub license](https://img.shields.io/github/license/MarkusAmshove/natls)

This repository contains a language server implementation for the [Natural language](https://en.wikipedia.org/wiki/ADABAS#Natural_(4GL)) created by Software AG.

The latest release and changelog can be found at [GitHub Releases](https://github.com/MarkusAmshove/natls/releases/).

The language server supports, but isn't limited to:

- Reporting syntax and linter diagnostics as you type
- Code completion
- Hover documentation of variables and modules
- Workspace symbols
- File structure
- Quickfixes and refactorings
- Outline
- Code Snippets
- Signature Help for modules
- [and many more](docs/lsp-features.md)

## Project state/limitations

The current state of the project is considered as early development.

The language server can be used for driving daily development in Natural, but it does have some limitations:

The parser is still [incomplete](docs/implemented-statements.md) and has some rough edges where the Natural language is context sensitive.
It also currently uses some hard coded assumptions about the language settings that should be configurable by the `.natural` file (like thousands seperators), which means that e.g. regional settings aren't considered.
Reporting Mode hasn't been considered yet, so currently only the structured mode syntax of statements is parsed correctly.

Some analyzers assume a coding style that might not fit your needs. Some of these are [configurable](docs/analyzer-config.md). If you're missing some options, feel free to open an issue.

## Contributing

Contributions in the form of code, issues and feature requests are always welcome.
Check out the [contribution guide](CONTRIBUTING.md) to find out more.

You can write your own analyzers, quickfixes and refactorings.
There is some guidance to follow along in the form of documentation:

- [Implementing Analyzers](docs/implementing-analyzers.md)
- [Implementing Quickfixes](docs/implementing-quickfixes.md)
- [Implementing Refactorings](docs/implementing-refactorings.md)

## Projects

This repository contains the following projects:

- `natparse`: Parser for the Natural language and project format
- `natlint`: Static code analysis
- `natls`: Language Server implementation using `natparse` and `natlint`
- `natqube`: Plugin for SonarQube which uses `natlint` to aggregate diagnostics and measures
- `natdoc` (planned): Javadoc-like documentation format to generate a static documentation site

## Building

All projects are written in Java and require the latest JDK to build and run. The only exception is `natqube` which requires Java 11 as SonarQube only supports running on Java 11 on the server side at the moment.

All projects are aggregated as Gradle modules into a single Gradle project.

To build the project and run tests use `./gradlew build`.

To create standalone jar files (fat jars that include all dependencies) run `./gradlew fatJar`.

## Running natlint

Natlint ships with a CLI interface to run it within a Natural project for in a CI pipeline.

To use the jar, simply run `java -jar natlint.jar` within your Natural project root directory.

The Natural plugin will autmatically be pulled from the backend.

To run via docker, use `docker run --rm -u $(id -u):$(id -g) -v $PWD:/work ghcr.io/markusamshove/natlint:latest`.

Given no arguments, the program will analyze all Natural sources that can be found in the Natural project of the working directory. The project is identified by either a `.natural` or `_naturalBuild` file.

The following flags can be used to narrow the scope down to a single or multiple files/libraries:

```shell
$ java -jar natls.jar --help

Usage: analyze [-hV] [--ci] [--fs] [-xlint] [-s=<minimumSeverity>]
               [--sink=<sinkType>] [-w=<workingDirectory>]
               [-d=<diagnosticIds>]... [-f=<qualifiedNames>]... [-g=<globs>]...
               [-l=<libraries>]... [-r=<relativePaths>]... [COMMAND]
Analyze the Natural project in the current working directory
      --ci                Analyzer will return exit code 0, even when
                            diagnostics are found. Will also use the CSV sink
  -d, --diagnostic=<diagnosticIds>
                          Filter out every diagnostic that does not match the
                            given id. Example: --diagnostic NLP011
  -f, --file=<qualifiedNames>
                          Only analyze modules matching any of the qualified
                            module name in the form of LIBRARY.MODULENAME (e.g.
                            LIB1.SUBPROG)
      --fs                Analyzer will create a csv with file statuses
  -g, --glob=<globs>      Only analyze modules that match the given glob
                            pattern.
  -h, --help              Show this help message and exit.
  -l, --library=<libraries>
                          Only analyze modules that reside in any of the given
                            libraries.
  -r, --relative=<relativePaths>
                          Only analyze modules matching any of the relative
                            paths. Path should be relative to project root.
  -s, --severity=<minimumSeverity>
                          Filter out diagnostics that are below the given
                            severity. Valid values: INFO, WARNING, ERROR
      --sink=<sinkType>   Sets the output sink where the diagnostics are
                            printed to. Defaults to STDOUT. Valid values:
                            STDOUT, NONE, CSV, CI_CSV
  -V, --version           Print version information and exit.
  -w, --workdir=<workingDirectory>
                          Sets the working directory to a different path than
                            the current one
      -xlint, --disable-linting
                          Skips analyzing with natlint
Commands:
  git-status  Analyze files from `git status --porcelain`
```

It is also possible to pipe the output from `git status` to natlint to automatically apply filters so that all files that have changes according to Git are linted:

`git status --porcelain | java -jar natlint.jar git-status`

Note: The flag `--porcelain` is mandatory.

`git-status` is a sub command of the default `analyze` command. This means that flags like `-xlint` have to be passed before `git-status`:

`git status --porcelain | java -jar natlint.jar -xlint git-status`


## Running natls

The language server is tested primarily with two clients:

- [vscode-natural](https://github.com/markusamshove/vscode-natural)
- neovim via `nvim-lspconfig`

## Running natqube

`natqube` requires at least SonarQube 9.9 (which is/was a LTS release).

The plugin is currently not distributed the SonarQube marketplace.
Starting from v0.10 you can grab the [natqube.jar from the Releases page](https://github.com/MarkusAmshove/natls/releases) and put it into the plugins folder as described in [the SonarQube documentation](https://docs.sonarqube.org/latest/setup-and-upgrade/install-a-plugin/#manually-installing-plugins).
Alternatively you can build the jar yourself (`gradlew fatJar`) and put the file from `libs/natqube/build/libs/natqube.jar` into the plugin folder.

With a running SonarQube instance, you can analyze your project by following these steps:

- Run natlint in CI mode through Docker or the jar file:
-- `java -jar natlint.jar --ci`
-- `docker run --rm -u $(id -u):$(id -g) -v $PWD:/work ghcr.io/markusamshove/natlint:latest --ci`
- Run the [SonarQube scanner](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner/) in your project directory

## Configuration

Some behavior of the Language Server (`natls`) can be controlled via settings that the Language Client sets. See [Language Server Configuration](docs/lsp-config.md) for settings.

The code analysis of `natlint` (and therefore `natqube` and `natls`) can be configured through an `.editorconfig` ([editorconfig.org](https://editorconfig.org/)) file.

This makes it possible to configure the severity of diagnostics and pass analyzer specific settings.

Example:

```editorconfig
[*]
natls.NL002.severity = none
```

The severity can be one of:

- `none`: Disables the diagnostic
- `info`
- `warn`
- `error`

Analyzer specific can be found in the [analyzer configuration documentation](docs/analyzer-config.md).

## Acknowledgements and dependencies

The logo was created by [Duffed](https://github.com/Duffed)

The language server uses an unmodified version of [lsp4j](https://github.com/eclipse/lsp4j).
