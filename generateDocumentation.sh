#!/usr/bin/env bash
set -eo pipefail

if [ "$1" == "" ];then
  echo "Path to website repository not provided"
  echo "This script will generate all necessary documentation for the website and copies it over into the repository"
  echo "./generateDocumentation.sh <path-to-website-repository>"
  exit 1
fi

set -u

docs_directory="$1/content/docs"
diagnostics_directory="$1/content/diagnostics"

./gradlew :ruletranslator:run
cp ./docs/*.md "$docs_directory"
find "$diagnostics_directory" -type f -name "N*.md" ! -name "_index.md" -delete
cp ./tools/ruletranslator/build/diagnostics/* "$diagnostics_directory"
