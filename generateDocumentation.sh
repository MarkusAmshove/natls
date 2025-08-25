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

find "$docs_directory" -type f -name "__*.md"  -delete
for f in ./docs/website/*.md; do
  cp -v "$f" "$docs_directory/__$(basename "${f}")"
done

./gradlew :ruletranslator:run
find "$diagnostics_directory" -type f -name "N*.md" ! -name "_index.md" -delete
cp ./tools/ruletranslator/build/diagnostics/* "$diagnostics_directory"
