/**
 * Tests for what a test class inherits from its parents. They cover the order of inherited
 * configuration methods, methods declared on a base class, and {@code dependsOnMethods} and {@code
 * alwaysRun} across a hierarchy.
 *
 * <p>Compare {@code org.testng.conffailure}, which is about a configuration method failing rather
 * than about where it was declared.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here.
 */
package org.testng.inheritance;
