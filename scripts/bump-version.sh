#!/bin/bash
set -e

version=$(grep 'version=' version.properties | cut -d'=' -f2)
IFS='.' read -r major minor patch <<< "$version"

if git log -1 --pretty=%B | grep -q "^feat:"; then
  minor=$((minor + 1))
  patch=0
elif git log -1 --pretty=%B | grep -q "^fix:"; then
  patch=$((patch + 1))
else
  echo "No version bump needed."
  exit 0
fi

new_version="$major.$minor.$patch"
echo "Bumping version to $new_version"

echo "version=$new_version" > version.properties

git config user.name "github-actions"
git config user.email "github-actions@github.com"
git commit -am "chore: bump version to $new_version"
git tag "v$new_version"
git push origin HEAD --tags
