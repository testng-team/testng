package org.testng.internal;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.internal.Parameters.FilterOutInJectedTypesResult;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.xml.XmlTest;

/** Provide test for package visible methods in org.testng.internal.Parameters */
public class ParametersTest {

  @Test
  @Parameters({"testdata"})
  public void filterOutInJectedTypesFromOptionalValuesTest(
      XmlTest xmlTest, @Optional("optionaltestdata") String testdata) {
    JDK15AnnotationFinder finder = new JDK15AnnotationFinder(null);
    Method curMethod = new Object() {}.getClass().getEnclosingMethod();
    FilterOutInJectedTypesResult filterOutResult =
        org.testng.internal.Parameters.filterOutInJectedTypesFromOptionalValues(
            curMethod.getParameterTypes(), finder.findOptionalValues(curMethod));
    Assert.assertEquals(filterOutResult.getOptionalValues()[0], "optionaltestdata");
    Assert.assertEquals(filterOutResult.getParameterTypes()[0], String.class);
  }
}
