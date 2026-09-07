/**
 * Tests for how TestNG describes a skipped test. They check the status it sets, and whether the
 * result says which failure caused the skip.
 *
 * <p>Compare {@code org.testng.conffailure}, which is about the configuration failure itself rather
 * than how the skip that follows is reported.
 *
 * <p>Classes handed to TestNG to produce that behavior live in {@code samples}, not here.
 */
package org.testng.skip;
