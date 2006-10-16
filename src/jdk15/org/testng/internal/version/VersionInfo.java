package org.testng.internal.version;

import org.testng.internal.AnnotationTypeEnum;

/**
 * <code>VersionInfo</code> helper class to statically obtain the 
 * TestNG version. 
 *
 * @author cquezel
 * @since 5.2
 */
public final class VersionInfo {
  private VersionInfo() {
    // Hide constructor
  }
  
  /** True if this is the JDK14 version of TestNG false if JDK5+ */
  public static final boolean IS_JDK14 = false;
  
  /**
   * Returns the default annotation type for this version of TestNG.
   * @return the default annotation type for this version of TestNG
   * @since 5.2
   */
  public static AnnotationTypeEnum getDefaultAnnotationType() {
    return AnnotationTypeEnum.JDK;
  }
}
