package test.dataprovider.issue2800;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public abstract class AbstractTestClassGenerator {

  @DataProvider(name = "dataProvider")
  public Object[][] dataProvider() {
    return new Object[][] {{"foo"}, {"bar"}};
  }

  @Factory(dataProvider = "dataProvider")
  public Object[] testFactory(String value) {
    return new Object[] {new TestClassSample()};
  }
}
