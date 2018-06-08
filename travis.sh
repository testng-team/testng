#!/bin/bash
set -e

if [ $1 == "sonarqube" ] ; then
  if [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ] ; then
    if [ -n "${TRAVIS_PULL_REQUEST}" -a "${TRAVIS_PULL_REQUEST}" != "false" ] ; then
      if [ -n "${SONAR_GITHUB_OAUTH}" -a -n "${SONAR_TOKEN}" ] ; then
        ./gradlew sonarqube \
          -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
          -Dsonar.github.oauth=$SONAR_GITHUB_OAUTH \
          -Dsonar.analysis.mode=issues \
          -Dsonar.login=${SONAR_TOKEN} \
          --stacktrace --info
      else
        echo "Sonar analyse skipped: No GitHub or Sonarqube token available"
      fi
    else
      if [ "${TRAVIS_BRANCH}" == "master" ] ; then
        if [ -n "${SONAR_TOKEN}" ] ; then
          ./gradlew sonarqube -Dsonar.login=${SONAR_TOKEN} --stacktrace --info
        else
          echo "Sonar analyse skipped: No Sonarqube token available"
        fi
      else
        echo "Sonar analyse skipped: Only on the master branch"
      fi
    fi
  else
    echo "Sonar analyse skipped: Only on Oracle JDK8"
  fi
else
  if [ $1 == "deploy" ] ; then
    if [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ] ; then
      if [ "${TRAVIS_PULL_REQUEST}" == "false" ] ; then
        if [ "${TRAVIS_BRANCH}" == "master" ] ; then
          ./gradlew uploadArchives --stacktrace --info
        else
          echo "Deploy skipped: Only the master branch"
        fi
      else
        echo "Deploy skipped: Not on pull request"
      fi
    else
      echo "Deploy skipped: Only on Oracle JDK7"
    fi
  else
    echo "Skipped: $1 is not a valid param"
  fi
fi
