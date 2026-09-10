/**
 * Input for the tests in {@code org.testng.inheritance}. A test builds a TestNG run around these
 * classes, then checks what happened.
 *
 * <p>These are not tests. They carry {@code @Test} methods, and the hierarchies they form are the
 * subject of the tests next door. Running them directly reports failures that mean nothing. The
 * build excludes this package from test discovery. Nothing here should ever be a root test.
 */
package org.testng.inheritance.samples;
