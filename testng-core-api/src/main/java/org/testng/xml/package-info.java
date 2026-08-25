/**
 * The suite model that testng.xml describes, its parser, and the weaver that writes it back.
 *
 * <p>Every value type here compares {@code getClass()} in {@code equals} rather than using {@code
 * instanceof}, and each one suppresses Error Prone's {@code EqualsGetClass} to say so.
 *
 * <p>{@code getClass()} keeps {@code equals} symmetric whatever a subclass does. {@code instanceof}
 * does not: a subclass that adds a value component and overrides {@code equals} makes {@code
 * base.equals(sub)} answer true while {@code sub.equals(base)} answers false, which is the contract
 * violation Effective Java describes. These types are public, not final, and are the model users
 * build a suite from programmatically -- {@code XmlTest} and {@code XmlClass} are extended in
 * TestNG's own suite, and the other five are as extensible -- so the base cannot assume no subclass
 * will do it. Sealing them instead would break every user who extends one.
 *
 * <p>No test in this repository fails if the comparison is changed. That is what makes it a
 * decision to record rather than a bug to fix, and why the check is an error now.
 *
 * <p>The suppression is per method on purpose: extracting the comparison into a shared helper would
 * stop the check firing at all, since it only looks inside an {@code equals} declaration, and would
 * disarm it silently for every value type added here afterwards.
 */
@NullMarked
package org.testng.xml;

import org.jspecify.annotations.NullMarked;
