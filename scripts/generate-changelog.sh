#!/bin/bash
set -e

# Generate changelog from git tags and conventional commits
echo "# Changelog" > CHANGELOG.md
echo "" >> CHANGELOG.md

tags=$(git tag --sort=-creatordate)

for tag in $tags; do
  echo "## $tag" >> CHANGELOG.md
  echo "" >> CHANGELOG.md
  git log "$tag^!" --pretty=format:"- %s" --grep="^feat\|^fix" >> CHANGELOG.md
  echo -e "\n" >> CHANGELOG.md
done
