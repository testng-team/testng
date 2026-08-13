package org.testng.annotations;

/**
 * Tri-state toggle used by {@link Factory#lazy()} to control whether a {@code @Factory} powered
 * test class should have its instances created lazily (just-in-time, right before the first
 * configuration/test method of the instance runs) instead of eagerly (all up-front, during test
 * collection).
 *
 * <p>The three states let the annotation participate in a resolution hierarchy where a more
 * granular level overrides a broader one: an explicit annotation value wins over the suite level
 * XML attribute, which in turn wins over the programmatic {@link org.testng.TestNG} configuration.
 * Only when the annotation is left as {@link #UNSET} do the suite/configuration levels take effect.
 */
public enum Lazy {

  /**
   * Force lazy (just-in-time) instantiation for this factory, regardless of suite/configuration.
   */
  TRUE,

  /** Force eager (up-front) instantiation for this factory, regardless of suite/configuration. */
  FALSE,

  /**
   * The attribute was not specified. Instantiation timing is resolved by the suite level attribute
   * and, failing that, the {@link org.testng.TestNG} configuration (defaulting to eager).
   */
  UNSET
}
