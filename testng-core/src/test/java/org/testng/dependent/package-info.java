/**
 * Tests for the order TestNG runs methods in when one waits for another. They cover {@code
 * dependsOnMethods} and {@code dependsOnGroups}, what happens when a dependency is missing, and how
 * {@code alwaysRun} and {@code ignoreMissingDependencies} change the outcome.
 *
 * <p>Compare {@code org.testng.priority}, which orders methods that do not wait for each other, and
 * {@code org.testng.preserveorder}, which keeps declaration order.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here.
 */
package org.testng.dependent;
