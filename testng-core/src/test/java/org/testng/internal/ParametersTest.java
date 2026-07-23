package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.internal.Parameters.FilterOutInjectedTypesResult;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.xml.XmlTest;

/** Provide test for package visible methods in org.testng.internal.Parameters */
public class ParametersTest {

  @Test
  @Parameters({"testdata"})
  @SuppressWarnings("unused")
  public void filterOutInJectedTypesFromOptionalValuesTest(
      XmlTest xmlTest, @Optional("optionaltestdata") String testdata) {
    JDK15AnnotationFinder finder = new JDK15AnnotationFinder(null);
    Method curMethod = new Object() {}.getClass().getEnclosingMethod();
    FilterOutInjectedTypesResult filterOutResult =
        org.testng.internal.Parameters.filterOutInjectedTypesFromOptionalValues(
            curMethod.getParameterTypes(), finder.findOptionalValues(curMethod));
    assertThat(filterOutResult.getOptionalValues()[0]).isEqualTo("optionaltestdata");
    assertThat(filterOutResult.getParameterTypes()[0]).isEqualTo(String.class);
  }
}
