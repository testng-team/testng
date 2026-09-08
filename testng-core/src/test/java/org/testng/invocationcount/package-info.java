/**
 * Tests for how often TestNG invokes a method, and how it counts those invocations. They cover
 * {@code invocationCount}, {@code skipFailedInvocations}, {@code successPercentage}, and the {@code
 * firstTimeOnly} and {@code lastTimeOnly} flags on configuration methods.
 *
 * <p>Compare {@code org.testng.priority}, which is about the order of methods rather than how many
 * times each one runs.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here.
 */
package org.testng.invocationcount;
