/**
 * Input for the tests in {@code org.testng.listeners}. A test builds a TestNG run around these
 * classes, then checks which listener calls happened.
 *
 * <p>Other features borrow from here too, because a listener fixture is useful beyond this one. So
 * a change here can break another feature's tests. To list those borrowers, run {@code grep -rl
 * org.testng.listeners.samples testng-core/src/test/java | grep -v /org/testng/listeners/}. The
 * second part drops every file of this feature, both these samples and the tests in {@code
 * org.testng.listeners}, so what is left is the users outside the feature. To list every user
 * instead, drop only {@code /org/testng/listeners/samples/}.
 *
 * <p>These are not tests. Some carry {@code @Test} methods, and some of those are written to fail.
 * Running them directly reports failures that mean nothing. {@code verifyTestExecution} fails the
 * build when a class under {@code .samples.} runs as a root test. The test task also excludes this
 * path, which is inert while the suite XML decides what runs and is there for the day it does not.
 */
package org.testng.listeners.samples;
