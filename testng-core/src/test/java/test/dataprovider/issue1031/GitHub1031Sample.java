package test.dataprovider.issue1031;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub1031Sample {

  @DataProvider
  public Object[][] tooFewForVarargs() {
    return new Object[][] {new Object[] {"only"}};
  }

  @Test(dataProvider = "tooFewForVarargs")
  public void varargsTooFew(String first, String... rest) {}

  @DataProvider
  public Object[][] nonEmptyRow() {
    return new Object[][] {new Object[] {"x"}};
  }

  @Test(dataProvider = "nonEmptyRow")
  public void noParameters() {}

  @DataProvider
  public Object[][] ints() {
    return new Object[][] {new Object[] {4}};
  }

  @Test(dataProvider = "ints")
  public void typeMismatch(String value) {}
}
