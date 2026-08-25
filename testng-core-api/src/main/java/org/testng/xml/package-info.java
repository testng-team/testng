/**
 * The suite model that testng.xml describes, its parser, and the weaver that writes it back.
 *
 * <p>Every value type here compares {@code getClass()} in {@code equals} rather than using {@code
 * instanceof}, and each one suppresses Error Prone's {@code EqualsGetClass} to say so. They are
 * public and not final, and users do extend them -- TestNG's own suite has an {@code XmlTest} and
 * an {@code XmlClass} subclass. {@code instanceof} would make such a subclass equal to its base
 * while the base stayed unequal to it, and sealing them instead would break every user who extends
 * one. The suppression is per method on purpose: extracting the comparison into a shared helper
 * would stop the check firing at all, since it only looks inside an {@code equals} declaration, and
 * would disarm it silently for every value type added here afterwards.
 */
@NullMarked
package org.testng.xml;

import org.jspecify.annotations.NullMarked;
