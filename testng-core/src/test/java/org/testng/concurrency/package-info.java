/**
 * Tests for how TestNG runs work at the same time: thread pools, parallel modes and thread counts.
 *
 * <p>This package is not {@code org.testng.thread}, and that is deliberate. The tests of every
 * other feature move from {@code test.<feature>} to {@code org.testng.<feature>}. These do not,
 * because the main source tree already has an {@code org.testng.thread} package, and it carries
 * {@code @NullMarked}. NullAway reads {@code @NullMarked} per package, not per source set. Test
 * classes in that package would be checked as if they were production code.
 *
 * <p>This file has no {@code @NullMarked} on purpose. Adding it would opt these tests back into the
 * check that this name exists to avoid.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here.
 */
package org.testng.concurrency;
