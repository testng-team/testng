/**
 * Input for the tests in {@code org.testng.conffailure}: classes handed to a programmatically built
 * TestNG run so the test can assert on what happened.
 *
 * <p>These are not tests. They carry {@code @Test} methods, and some of them are meant to fail or
 * to be skipped, so running them directly reports failures that mean nothing. The build excludes
 * this package from test discovery; nothing here should ever be a root test.
 */
package org.testng.conffailure.samples;
