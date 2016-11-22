package org.testng;

/**
 * This interface lets you alter the test name of an {@link ITestResult} object.
 * Sample : <br>
 *
 * <pre>
 *     if (testResult instanceOf IAlterTestName) {
 *         ((ITestResult) testResult).setTestName(newName);
 *     }
 *
 * </pre>
 */
public interface IAlterTestName {
    /**
     * @param name - The new name to be used as a test name
     */
    void setTestName(String name);
}
