# Need help? 
Before opening a new issue, did you ask your question on

* [Google group](https://groups.google.com/group/testng-users)
* [StackOverflow](https://stackoverflow.com/questions/tagged/testng)

If you posted on both sites, please provide the link to the other question to avoid duplicating the answer.

# Are you sure it is a TestNG bug?
Before posting the issue, try to reproduce the issue in [a shell window](https://testng.org/doc/documentation-main.html#running-testng).

If the problem does not exist with the shell, first check if the issue exists on the bugtracker of the runner, and open an issue there first:

* Eclipse: https://github.com/cbeust/testng-eclipse/issues
* IntelliJ: [https://youtrack.jetbrains.com/issues](https://youtrack.jetbrains.com/issues?q=%23%7BUnit%20Testing.%20TestNG%7D%20)
* Maven: https://issues.apache.org/jira/browse/SUREFIRE
* Gradle: https://issues.gradle.org/projects/GRADLE

# Which version are you using?
Always make sure your issue is happening on the latest TestNG version. Bug reports occurring on older versions will not be looked at quickly.

# Have you considered sending a pull request instead of filing an issue?

The best way to report a bug is to provide the TestNG team with a full test case reproducing the issue.
Maybe you can write a runnable test case (check the `src/test/` folder for examples) and propose it in a pull request 
Don't worry if the CI fails because it is the expected behavior.
This pull request will be a perfect start to find the fix :)

# We encourage pull requests that:
  
  * Add new features to TestNG (or)
  * Fix bugs in TestNG
  
  If your pull request involves fixing SonarQube issues then we would suggest that you please discuss this with the 
  [TestNG-dev](https://groups.google.com/forum/#!forum/testng-dev) before you spend time working on it.

# Working with the codebase

### List all tasks

After cloning the project, run `./gradlew tasks` to list out all the tasks that are available.

### Importing into IntelliJ

To import the TestNG project into IntelliJ run `./gradlew openIdea`

### Running all tests

To run all the test cases (build the code) run `./gradlew test`

### Running tests that belong to a specific module

* First list the sub-modules that are available in the TestNG codebase by running `./gradlew projects`
* Now you can run tests that belong to any specific module by running `./gradlew :<moduleNameGoesHere>:test`. For e.g., to run tests under the sub-module **testng-asserts** run the command : `./gradlew :testng-asserts:test`

The codebase makes use of [autostyle](https://github.com/autostyle/autostyle) to enforce code formatting and also fixing code formatting.

To check if there are any code formatting/styling issues (Its best to run this before every pull request), run `./gradlew autostyleCheck`

To automatically fix any styling issues run `./gradlew autostyleApply`

**Tip:**

In order to automatically have the formatting applied as and when you commit code, you can do the following:

1. Create a new file under `.git/hooks/pre-commit`
2. Now add the below contents to this file and save it.

```
# run the auto style applying on the code
./gradlew autostyleApply

# store the last exit code in a variable
RESULT=$?

# return the './gradlew autostyleApply' exit code
exit $RESULT
```

This will cause git to trigger the code styling automatically right before commiting your changes locally.

After that you can quickly check if there were any formatting that was applied using `git status`
