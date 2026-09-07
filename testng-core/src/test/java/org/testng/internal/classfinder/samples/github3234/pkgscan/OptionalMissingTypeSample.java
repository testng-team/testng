package org.testng.internal.classfinder.samples.github3234.pkgscan;

import org.testng.internal.classfinder.samples.github3234.MissingType;

/**
 * An optional class in the scanned package. One method names {@link MissingType}, so reading the
 * methods throws {@code NoClassDefFoundError} when that type is missing.
 */
public class OptionalMissingTypeSample {

  public MissingType unused() {
    return null;
  }
}
