---
title: "NatLint CLI"
slug: natlint-cli
weight: 3
---

NatLint ships with a CLI interface to run it within a Natural project for in a CI pipeline.

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

## Analyze changed files

It is possible to pipe the output from `git status` to natlint to automatically apply filters so that all files that have changes according to Git are linted:

`git status --porcelain | java -jar natlint.jar git-status`

Note: The flag `--porcelain` is mandatory.

`git-status` is a sub command of the default `analyze` command. This means that flags like `-xlint` have to be passed before `git-status`:

`git status --porcelain | java -jar natlint.jar -xlint git-status`
