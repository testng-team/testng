/**
 * Tests for group membership and selection: how {@code groups} on {@code @Test} is inherited,
 * included and excluded, and how group filters interact with the rest of the run.
 *
 * <p>Compare {@code org.testng.aftergroups}, which is about the configuration methods bound to a
 * group rather than about selecting one.
 *
 * <p>Classes handed to TestNG to produce that behaviour live in {@code samples}, not here.
 */
package org.testng.groups;
