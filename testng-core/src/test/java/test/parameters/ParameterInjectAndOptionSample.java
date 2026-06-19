package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

public class ParameterInjectAndOptionSample {

  @BeforeSuite
  @Parameters({"beforesuitedata"})
  public void beforeSuite(
      ITestContext context, @Optional("optionalbeforesuitedata") String beforesuitedata) {
    assertThat(beforesuitedata).isEqualTo("optionalbeforesuitedata");
  }

  @Test
  @Parameters({"testdata"})
  public void test(XmlTest xmlTest, @Optional("optionaltestdata") String testdata) {
    assertThat(testdata).isEqualTo("optionaltestdata");
  }
}
