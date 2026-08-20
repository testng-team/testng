/**
 * Reporting state TestNG produces for its own built-in reporters, shared by all of them rather than
 * rebuilt by each.
 *
 * <p>Internal: this package is deliberately absent from the {@code Export-Package} list in {@code
 * testng/testng-build.gradle.kts}, which is written out by hand. Its classes are in the jar and
 * visible to every built-in reporter -- {@code org.testng.reporters} and {@code
 * org.testng.reporters.jq} alike -- but no OSGi bundle can import them, and nothing here is API.
 */
@NullMarked
package org.testng.internal.reporters;

import org.jspecify.annotations.NullMarked;
