/**
 * Tests for listeners: how TestNG finds them, in what order it calls them, and how many times.
 *
 * <p>Compare {@code org.testng.methodinterceptors}, which is about a method interceptor choosing
 * and ordering test methods. Compare {@code org.testng.reporters} for the listeners that write
 * reports.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here. Some
 * tests keep such a class nested inside themselves instead. Search this package for {@code public
 * static class} to find them. A nested class that carries {@code @Test} methods is reported as a
 * class of its own, so {@code testng-core/execution-inventory.txt} names it.
 *
 * <p>{@code testng-test-kit} and {@code testng-jcommander} hold their own {@code test.listeners}
 * packages. Those belong to other modules and do not move with this one.
 */
package org.testng.listeners;
