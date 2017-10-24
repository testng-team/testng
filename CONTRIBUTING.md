## Contributing to TestNG

Please follow the steps below:

1. Install git by following the instructions in [this](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) link.
2. Set up your mail and name in git. Please refer [here](https://help.github.com/articles/setting-your-email-in-git/) on how to do it.
3. Fork the TestNG repository [ This is done ONLY once ]. Here’s a [good article](https://help.github.com/articles/fork-a-repo/) that will teach you what you need to know about forking a repository in Github. 
4. Clone your forked repository [ This is ALSO done ONLY once ]. [Here’s](https://help.github.com/articles/fork-a-repo/) how to do it. 
5. Now from within your clone, create a new branch to represent your changes. [Here’s](https://www.atlassian.com/git/tutorials/using-branches) how to do it.
6. If it's a new feature please make sure you have a discussion on it with the TestNG team first before you start work or you may end up spending time unnecessarily. Once you have a buy in, make sure you create a [new issue](https://github.com/cbeust/testng/issues/new) in the TestNG repository.
7. Now add your code, add javadocs, don't forget unit tests for your delivery and then also update the [CHANGES.txt](https://github.com/cbeust/testng/blob/master/CHANGES.txt). Unit tests reference should be added to [src/test/resources/testng.xml](https://github.com/cbeust/testng/blob/master/src/test/resources/testng.xml)
8. Include a good commit message. [Here’s](https://github.com/erlang/otp/wiki/Writing-good-commit-messages) how to do it.
9. Make sure you refer to the issue that you created on github in your commit message. This will ensure that the reviewers of your delivery will be able to tie down your delivery to the issue that it's fixing. [Here’s](https://guides.github.com/features/issues/) how to do it.
10. Now from your clone run the command : `git push origin my-new-feature` [ here *my-new-feature* is the name of the new branch being created for our new testng delivery/bugfix etc., Please replace it with your branch name ]
11. Send a pull request which is the last step of your delivery. [Here’s](https://help.github.com/articles/creating-a-pull-request/) how to do it.

Remember : From your second delivery onwards, you just need to start following steps **(5) to (11)**.

To run the build, from the command prompt you just need to invoke `./build-with-gradle`. 

If you are on windows, then you can perhaps just invoke `gradlew --daemon --stacktrace clean build test`

