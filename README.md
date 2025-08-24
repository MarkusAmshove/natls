# Natural Language Server (natls)

![NatLS Logo](assets/logo_128x128.png)

[![Continuous Integration](https://github.com/MarkusAmshove/natlint-manual/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/MarkusAmshove/natlint-manual/actions/workflows/gradle-build.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=MarkusAmshove_natls&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=MarkusAmshove_natls) [![Documentation](https://img.shields.io/badge/docs-latest-blue.svg)](https://nat-ls.github.io/) ![GitHub license](https://img.shields.io/github/license/MarkusAmshove/natls)

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
- [and many more](https://nat-ls.github.io/docs/lsp-features/)

## Project state/limitations

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

## Running natls

The language server is tested primarily with two clients:

- [vscode-natural](https://github.com/markusamshove/vscode-natural)
- neovim via `nvim-lspconfig`

## Acknowledgements and dependencies

The logo was created by [Duffed](https://github.com/Duffed)

The language server uses an unmodified version of [lsp4j](https://github.com/eclipse/lsp4j).
