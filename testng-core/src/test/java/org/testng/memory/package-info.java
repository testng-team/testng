/**
 * Tests that a finished run releases what it allocated: no test instance, listener or TestNG object
 * stays reachable once the suite is over.
 *
 * <p>These do not map onto a TestNG feature. They assert an operational property of the engine, so
 * they need a package of their own rather than a home under whichever feature happened to leak.
 *
 * <p>Classes handed to TestNG to produce that behaviour live in {@code samples}, not here.
 */
package org.testng.memory;
