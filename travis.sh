#!/bin/bash

if [ -n "$SONAR_GITHUB_OAUTH" ]; then
  ./gradlew sonarqube \
    -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
    -Dsonar.github.oauth=$SONAR_GITHUB_OAUTH \
    -Dsonar.analysis.mode=issue
else
  echo "No oauth token available"
fi
