package test.dataprovider.issue1987;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.IDataProviderMethod;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(dataProvider = "testData", description = "GITHUB-1987")
  public void extractDataProviderInfoWhenDpResidesInSameClass(
      Class<?> clazz, boolean performInstanceCheck, Class<?> dataProviderClass) {
    TestNG testng = create(clazz);
    DataProviderTrackingListener listener = new DataProviderTrackingListener();
    testng.addListener(listener);
    testng.run();
    ITestNGMethod method = listener.getResult().getMethod();
    IDataProviderMethod dpm = method.getDataProviderMethod();
    assertThat(dpm).isNotNull();
    if (performInstanceCheck) {
      assertThat(dpm.getInstance()).isEqualTo(method.getInstance());
    }
    assertThat(dpm.getMethod().getName()).isEqualTo("getData");
    assertThat(dpm.getInstance().getClass()).isEqualTo(dataProviderClass);
  }

  @DataProvider(name = "testData")
  public Object[][] getTestData() {
    return new Object[][] {
      {DataProviderInSameClass.class, true, DataProviderInSameClass.class},
      {DataProviderInBaseClass.class, true, DataProviderInBaseClass.class},
      {DataProviderInDifferentClass.class, false, BaseClassSample.class}
    };
  }
}
