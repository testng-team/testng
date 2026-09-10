/**
 * Input for the tests in {@code org.testng.dependent}. A test builds a TestNG run around these
 * classes, then checks what happened.
 *
 * <p>These are not tests. They carry {@code @Test} methods, and several name a method or a group
 * that does not exist, so TestNG refuses the run. Running them directly reports failures that mean
 * nothing. The build excludes this package from test discovery. Nothing here should ever be a root
 * test.
 */
package org.testng.dependent.samples;
