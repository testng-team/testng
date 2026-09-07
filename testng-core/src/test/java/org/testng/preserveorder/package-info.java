/**
 * Tests for {@code preserve-order} in a suite file. They check that TestNG runs classes and methods
 * in the order the file lists them.
 *
 * <p>Compare {@code org.testng.priority}, which orders by a number on the annotation instead.
 *
 * <p>Classes handed to TestNG to produce that behaviour live in {@code samples}, not here.
 */
package org.testng.preserveorder;
