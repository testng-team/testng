/**
 * Tests for {@code @BeforeGroups} and {@code @AfterGroups}: when the group-level configuration
 * methods fire, what happens when a member of the group is skipped, fails or is removed by a method
 * interceptor, and how {@code alwaysRun} affects them.
 *
 * <p>Classes handed to TestNG to produce that behaviour live in {@code samples}, not here.
 */
package org.testng.aftergroups;
