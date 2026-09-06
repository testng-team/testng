/**
 * Tests for what TestNG does when a configuration method fails: which tests are skipped, what is
 * reported for them, and how the failure propagates from a suite, test or class level
 * {@code @Before*} method.
 *
 * <p>Compare {@code org.testng.skip}, which is about how a skip is described once it has happened.
 *
 * <p>Classes handed to TestNG to produce that behaviour live in {@code samples}, not here.
 */
package org.testng.conffailure;
