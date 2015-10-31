#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  if [ -n "$SONAR_GITHUB_OAUTH" ]; then
    ./gradlew sonarqube \
      -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
      -Dsonar.github.oauth=$SONAR_GITHUB_OAUTH \
  else
    echo "No oauth token available"
  fi
fi