/**
 * Tests for configuration methods: {@code @BeforeSuite} to {@code @AfterMethod}, their order, their
 * groups and their inheritance.
 *
 * <p>Compare {@code org.testng.conffailure}, which is about what TestNG does when a configuration
 * method fails. Compare {@code org.testng.inheritance} for the order of inherited methods of every
 * kind, and {@code org.testng.aftergroups} for {@code @BeforeGroups} and {@code @AfterGroups}.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here. The one
 * exception is {@code issue2209.IssueTest}, which keeps its sample as a nested class.
 */
package org.testng.configuration;
