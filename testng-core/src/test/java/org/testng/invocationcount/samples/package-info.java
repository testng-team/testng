/**
 * Input for the tests in {@code org.testng.invocationcount}. A test builds a TestNG run around
 * these classes, then checks what happened.
 *
 * <p>These are not tests. They carry {@code @Test} methods, and many are written to fail so that
 * the invocation that follows is cancelled. Running them directly reports failures that mean
 * nothing. The build excludes this package from test discovery. Nothing here should ever be a root
 * test.
 */
package org.testng.invocationcount.samples;
