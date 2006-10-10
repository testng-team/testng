/*
 * @(#)AnnotationType.java
 *
 * Copyright 1999-2004 by Taleo Corporation,
 * 330 St-Vallier East, Suite 400, Quebec city, Quebec, G1K 9C5, CANADA
 * All rights reserved
 */
package org.testng.internal;

import org.testng.internal.version.VersionInfo;
import org.testng.log4testng.Logger;

/**
 * <code>AnnotationTypeEnum</code> is an enumeration value representing the annotation type. 
 * @author cquezel
 */
public final class AnnotationTypeEnum {
  /** This class's log4testng Logger. */
  private static final Logger LOGGER = Logger.getLogger(AnnotationTypeEnum.class);
  
  /** The JDK50 annotation type ID ("JDK5").*/
  private static final String JDK5_ANNOTATION_TYPE = "JDK5";
  
  /** The JavaDoc annotation type ID ("javadoc"). */
  private static final String JAVADOC_ANNOTATION_TYPE = "javadoc";

  /** javadoc annotation type */
  public static final AnnotationTypeEnum JAVADOC = new AnnotationTypeEnum(JAVADOC_ANNOTATION_TYPE);
  
  /** JDK5 annotation type */
  public static final AnnotationTypeEnum JDK5 = new AnnotationTypeEnum(JDK5_ANNOTATION_TYPE);
  
  /** The enumeration name (one of JAVADOC_ANNOTATION_TYPE or JDK5_ANNOTATION_TYPE) */
  private String m_name;

  /**
   * Returns an <code>AnnotationTypeEnum</code> object holding the value of the 
   * specified <code>pAnnotationType</code>. This method throws an IllegalArgumentException
   * if pAnnotationType is an illegal value or if this is version 14 and JDK5 annotations
   * are specified.
   * 
   * @param pAnnotationType the annotation type. This is one of the two constants 
   * (AnnotationTypeEnum.JAVADOC_ANNOTATION_TYPE or AnnotationTypeEnum.JDK5_ANNOTATION_TYPE).
   * For backward compatibility we accept "1.4", "1.5". Any other value will default to 
   * AnnotationTypeEnum.JDK5 if this is the 1.5 version of TestNG or
   * AnnotationTypeEnum.JAVADOC if this is the 1.4 version of TestNG. 
   * 
   * @return an <code>AnnotationTypeEnum</code> object holding the value of the 
   * specified <code>pAnnotationType</code>. 
   */
  public static AnnotationTypeEnum valueOf(String pAnnotationType) {
    if (pAnnotationType == null) {
      throw new IllegalArgumentException("pAnnotationType is null");
    }
    
    AnnotationTypeEnum annotationType;
    if (pAnnotationType.equals(JAVADOC_ANNOTATION_TYPE)) {
      annotationType = JAVADOC;
    }
    else if (pAnnotationType.equals(JDK5_ANNOTATION_TYPE)) {
      annotationType = JDK5;
    }
    else if (pAnnotationType.equals("1.4") 
        || pAnnotationType.toLowerCase().equals(JAVADOC_ANNOTATION_TYPE.toLowerCase())) {
      // For backward compatibility only
      annotationType = JAVADOC;
      log(pAnnotationType, annotationType);
    }
    else if ("1.5".equals(pAnnotationType) 
        || pAnnotationType.toLowerCase().equals(JDK5_ANNOTATION_TYPE.toLowerCase())) {
      // For backward compatibility only
      annotationType = JDK5;
      log(pAnnotationType, annotationType);
    }
    else if (pAnnotationType.toLowerCase().equals("jdk15")) {
      // For backward compatibility only
      annotationType = JDK5;
      log(pAnnotationType, annotationType);
    }
    else {
      // For backward compatibility only
      // TODO should we make this an error?
      annotationType = VersionInfo.getDefaultAnnotationType();
      log(pAnnotationType, annotationType);
    }
    
    if (VersionInfo.IS_JDK14 && annotationType == JDK5) {
      throw new IllegalArgumentException(
          "Cannot specify \"" + pAnnotationType + "\" with 1.4 version of TestNG");
    }
    return annotationType;
  }

  /**
   * Returns a human readable representation of the enum, suitable to be converted
   * back to the enumeration by the valueOf method.
   * @return a human readable representation of the enum, suitable to be converted
   * back to the enumeration by the valueOf method.
   */
  public String getName() {
    return m_name;
  }
  
  /**
   * Returns the m_name of the annotation type.
   * {@inheritDoc}
   */
  @Override 
  public String toString() {
    return m_name;
  }

  
  private static void log(String pAnnotationType, AnnotationTypeEnum pDefault) {
    String msg = "Illegal pAnnotationType type \"" + pAnnotationType 
      + "\" defaulting to \"" + pDefault + "\"";
    LOGGER.info(msg);
    Utils.log("", 2, msg);
  }
  
  private AnnotationTypeEnum(String pName) {
    m_name = pName;
  }
}
