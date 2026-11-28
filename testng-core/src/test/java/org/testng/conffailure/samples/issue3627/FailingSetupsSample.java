package org.testng.conffailure.samples.issue3627;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

/** Five equal instances. The {@code @BeforeMethod} method of each one fails. */
public class FailingSetupsSample extends EqualInstancesSample {

  @Factory(dataProvider = "instances")
  public FailingSetupsSample(String name, boolean skip) {
    super(name, skip);
  }

  @DataProvider
  public static Object[][] instances() {
    return new Object[][] {
      {"first", false}, {"second", false}, {"third", false}, {"fourth", false}, {"fifth", false}
    };
  }
}
