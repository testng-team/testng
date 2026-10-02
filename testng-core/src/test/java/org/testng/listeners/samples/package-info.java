/**
 * Input for the tests in {@code org.testng.listeners}. A test builds a TestNG run around these
 * classes, then checks which listener calls happened.
 *
 * <p>Other features borrow from here too, because a listener fixture is useful beyond this one. So
 * a change here can break another feature's tests. To list the borrowers, run {@code grep -rl
 * org.testng.listeners.samples testng-core/src/test/java | grep -v /org/testng/listeners/} — the
 * second part drops this package's own files, which match their own package line.
 *
 * <p>These are not tests. Some carry {@code @Test} methods, and some of those are written to fail.
 * Running them directly reports failures that mean nothing. {@code verifyTestExecution} fails the
 * build when a class under {@code .samples.} runs as a root test. The test task also excludes this
 * path, which is inert while the suite XML decides what runs and is there for the day it does not.
 */
package org.testng.listeners.samples;
