[![Maven Central](https://img.shields.io/maven-central/v/org.testng/testng.svg)](https://maven-badges.herokuapp.com/maven-central/org.testng/testng)
[![License](https://img.shields.io/github/license/cbeust/testng.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Sonarqube tech debt](https://img.shields.io/sonar/https/sonarqube.com/org.testng:testng/tech_debt.svg?label=Sonarqube%20tech%20debt)](https://sonarqube.com/dashboard/index?id=org.testng:testng)
[![Sonarqube Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.testng%3Atestng&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.testng%3Atestng)

Documentation available at [TestNG's main web site](https://testng.org). Visit [TestNG Documentation's GitHub Repo](https://github.com/testng-team/testng-team.github.io) to contribute to it.

### Release Notes
* [7.9.0](https://groups.google.com/g/testng-users/c/nN7LkuZWO48)
* [7.8.0](https://groups.google.com/g/testng-users/c/xdldK3VyU_s)
* [7.7.0](https://groups.google.com/g/testng-users/c/V6jie-9uUIA)
* [7.6.0](https://groups.google.com/g/testng-users/c/BAFB1vk-kok)
* [7.5](https://groups.google.com/g/testng-users/c/ESLiK8xSomc)
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

* Eclipse	:[Issues Page](https://github.com/cbeust/testng-eclipse/issues)
* IntelliJ:[Issues Page](https://youtrack.jetbrains.com/issues?q=Subsystem:%20%7BJava.%20Tests.%20TestNG%7D)
* Maven	:[Issues Page](https://issues.apache.org/jira/browse/SUREFIRE)
* Gradle	:[Issues Page](https://issues.gradle.org/projects/GRADLE)

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
  
### GPG Keys

#### Getting the keys

Download the keys as shown below:

```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys 0F13D5631D6AF36D
gpg: key 0F13D5631D6AF36D: "Krishnan Mahadevan (krmahadevan-key) <krishnan.mahadevan1978@gmail.com>" not changed
gpg: Total number processed: 1
gpg:              unchanged: 1
```

#### Trusting the keys

Trust the keys as shown below:

```bash
gpg --edit-key 0F13D5631D6AF36D
gpg (GnuPG) 2.4.4; Copyright (C) 2024 g10 Code GmbH
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Secret key is available.

sec  rsa2048/0F13D5631D6AF36D
     created: 2016-12-01  expires: never       usage: SC
     trust: full          validity: unknown
ssb  rsa2048/7295B61CC8DD9AE8
     created: 2016-12-01  expires: never       usage: E
[ unknown] (1). Krishnan Mahadevan (krmahadevan-key) <krishnan.mahadevan1978@gmail.com>

gpg> trust
sec  rsa2048/0F13D5631D6AF36D
     created: 2016-12-01  expires: never       usage: SC
     trust: full          validity: unknown
ssb  rsa2048/7295B61CC8DD9AE8
     created: 2016-12-01  expires: never       usage: E
[ unknown] (1). Krishnan Mahadevan (krmahadevan-key) <krishnan.mahadevan1978@gmail.com>

Please decide how far you trust this user to correctly verify other users' keys
(by looking at passports, checking fingerprints from different sources, etc.)

  1 = I don't know or won't say
  2 = I do NOT trust
  3 = I trust marginally
  4 = I trust fully
  5 = I trust ultimately
  m = back to the main menu

Your decision? 5
Do you really want to set this key to ultimate trust? (y/N) y

sec  rsa2048/0F13D5631D6AF36D
     created: 2016-12-01  expires: never       usage: SC
     trust: ultimate      validity: unknown
ssb  rsa2048/7295B61CC8DD9AE8
     created: 2016-12-01  expires: never       usage: E
[ unknown] (1). Krishnan Mahadevan (krmahadevan-key) <krishnan.mahadevan1978@gmail.com>
Please note that the shown key validity is not necessarily correct
unless you restart the program.

gpg> exit

Invalid command  (try "help")

gpg> quit
```

#### Verifying the signature

1. Download the `.asc` file from `https://repo1.maven.org/maven2/org/testng/testng/<versionGoesHere>`
2. Run the command `gpg --verify testng-<versionGoesHere>.jar.asc testng-<versionGoesHere>.jar`
3. You should see an output as below:

```bash
gpg: Signature made Tue Dec 26 15:06:16 2023 IST
gpg:                using RSA key 0F13D5631D6AF36D
gpg: checking the trustdb
gpg: marginals needed: 3  completes needed: 1  trust model: pgp
gpg: depth: 0  valid:   1  signed:   0  trust: 0-, 0q, 0n, 0m, 0f, 1u
gpg: Good signature from "Krishnan Mahadevan (krmahadevan-key) <krishnan.mahadevan1978@gmail.com>" [ultimate]
```

For more details regarding keys please refer:

* [Verifying Signature](https://infra.apache.org/release-signing.html#verifying-signature)
* [How to Trust Imported GPG Keys](https://classroom.anir0y.in/post/blog-how-to-trust-imported-gpg-keys/)