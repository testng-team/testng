[![Build Status](http://img.shields.io/travis/cbeust/testng.svg)](https://travis-ci.org/cbeust/testng)
[![Dependency Status](https://www.versioneye.com/user/projects/576aecf1c2ea9d00096bf58f/badge.svg)](https://www.versioneye.com/user/projects/576aecf1c2ea9d00096bf58f)
[![Reference Status](https://www.versioneye.com/java/org.testng:testng/reference_badge.svg)](https://www.versioneye.com/java/org.testng:testng/references)
[![Maven Central](https://img.shields.io/maven-central/v/org.testng/testng.svg)](https://maven-badges.herokuapp.com/maven-central/org.testng/testng)
[![Bintray](https://api.bintray.com/packages/cbeust/maven/testng/images/download.svg)](https://bintray.com/cbeust/maven/testng/_latestVersion)
[![License](https://img.shields.io/github/license/cbeust/testng.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Sonarqube tech debt](https://img.shields.io/sonar/https/sonarqube.com/org.testng:testng/tech_debt.svg?label=Sonarqube%20tech%20debt)](https://sonarqube.com/dashboard/index?id=org.testng:testng)
[![Sonarqube quality gate](https://sonarqube.com/api/badges/gate?key=org.testng:testng)](https://sonarqube.com/dashboard/index?id=org.testng:testng)

Documentation available at [TestNG's main web site](http://testng.org).

### Need help?
Before opening a new issue, did you ask your question on
* [Google group](http://groups.google.com/group/testng-users)
* [StackOverflow](http://stackoverflow.com/questions/tagged/testng)

If you posted on both sites, please provide the link to the other question to avoid duplicating the answer.

### Are you sure it is a TestNG bug?
Before posting the issue, try to reproduce the issue in [a shell window](http://testng.org/doc/documentation-main.html#running-testng).

If the problem does not exist with the shell, first check if the issue exists on the bugtracker of the runner, and open an issue there first:
* Eclipse: https://github.com/cbeust/testng-eclipse/issues
* IntelliJ: [https://youtrack.jetbrains.com/issues](https://youtrack.jetbrains.com/issues?q=%23%7BUnit%20Testing.%20TestNG%7D%20)
* Maven: https://issues.apache.org/jira/browse/SUREFIRE
* Gradle: https://issues.gradle.org/projects/GRADLE

### Which version are you using?
Always make sure your issue is happening on the latest TestNG version. Bug reports occurring on older versions will not be looked at quickly.

### Have you considered sending a pull request instead of filing an issue?
The best way to report a bug is to provide the TestNG team with a full test case reproducing the issue.
Maybe you can write a runnable test case (check the `src/test/` folder for examples) and propose it in a pull request 
Don't worry if the CI fails because it is the expected behavior.
This pull request will be a perfect start to find the fix :)

### How to create a pull request?
Refer our [Contributing](./CONTRIBUTING.md) section for detailed set of steps.

### We encourage pull requests that:

  * Add new features to TestNG (or)
  * Fix bugs in TestNG

  If your pull request involves fixing SonarQube issues then we would suggest that you please discuss this with the 
  [TestNG-dev](https://groups.google.com/forum/#!forum/testng-dev) before you spend time working on it.
