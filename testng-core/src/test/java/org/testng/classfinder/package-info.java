/**
 * Tests for how TestNG decides whether a loaded class is a test class: annotation inspection in
 * {@code TestNGClassFinder}, and what happens when that inspection cannot read the methods.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here.
 */
package org.testng.classfinder;
