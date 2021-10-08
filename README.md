[![Maven Central](https://img.shields.io/maven-central/v/org.testng/testng.svg)](https://maven-badges.herokuapp.com/maven-central/org.testng/testng)
[![License](https://img.shields.io/github/license/cbeust/testng.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Sonarqube tech debt](https://img.shields.io/sonar/https/sonarqube.com/org.testng:testng/tech_debt.svg?label=Sonarqube%20tech%20debt)](https://sonarqube.com/dashboard/index?id=org.testng:testng)
[![Sonarqube Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.testng%3Atestng&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.testng%3Atestng)

Documentation available at [TestNG's main web site](https://testng.org).

### Release Notes

* [7.4.0](https://groups.google.com/g/testng-users/c/dwSJ04qeu8k)
* [7.3.0](https://groups.google.com/forum/#!topic/testng-users/a81uaZvtEZI)
* [7.1.0](https://groups.google.com/forum/#!topic/testng-users/84bYPJ1rjno)
* [7.0.0](https://groups.google.com/forum/#!topic/testng-users/HKujuefBhXA)

### Need help?
Before opening a new issue, did you ask your question on
* [Google group](https://groups.google.com/group/testng-users)
* [StackOverflow](https://stackoverflow.com/questions/tagged/testng)

If you posted on both sites, please provide the link to the other question to avoid duplicating the answer.

### Are you sure it is a TestNG bug?
Before posting the issue, try to reproduce the issue in [a shell window](https://testng.org/doc/documentation-main.html#running-testng).

If the problem does not exist with the shell, first check if the issue exists on the bugtracker of the runner, and open an issue there first:
* Eclipse: https://github.com/cbeust/testng-eclipse/issues
* IntelliJ: [https://youtrack.jetbrains.com/issues](https://youtrack.jetbrains.com/issues?q=Subsystem:%20%7BJava.%20Tests.%20TestNG%7D)
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
Refer our [Contributing](.github/CONTRIBUTING.md) section for detailed set of steps.

### We encourage pull requests that:

  * Add new features to TestNG (or)
  * Fix bugs in TestNG

  If your pull request involves fixing SonarQube issues then we would suggest that you please discuss this with the 
  [TestNG-dev](https://groups.google.com/forum/#!forum/testng-dev) before you spend time working on it.
