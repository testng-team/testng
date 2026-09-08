/**
 * Input for the tests in {@code org.testng.dataprovider}: classes handed to a programmatically
 * built TestNG run so the test can assert on what happened.
 *
 * <p>These are not tests. They carry {@code @Test} methods, and most of them are meant to be
 * skipped or to fail, so running them directly reports failures that mean nothing. The build
 * excludes this package from test discovery; nothing here should ever be a root test.
 */
package org.testng.dataprovider.samples;
