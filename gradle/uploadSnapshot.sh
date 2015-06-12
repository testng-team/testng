#!/bin/sh

echo "TRAVIS_HDK_HOME" $TRAVIS_JDK_HOME

if [ $TRAVIS_JDK_HOME == 'oraclejdk7' ]
then
  echo "Uploading snapshot"
  ./gradlew uploadArchives
else
  echo "Current JDK is ${TRAVIS_JDK_HOME}, not uploading snapshot"
fi

