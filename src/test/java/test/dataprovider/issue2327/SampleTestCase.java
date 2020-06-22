package test.dataprovider.issue2327;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(CustomListener.class)
public class SampleTestCase {

  @BeforeMethod(alwaysRun = true)
  public void setup(Method method, Object[] params) {
    if (Arrays.asList(params).contains("Dataset1")) {
      throw new RuntimeException("setup fail.");
    }
  }

  @DataProvider(name = "dp", parallel = true)
  public Iterator<Object> dp() {
    LinkedList<Object> objects = new LinkedList<>();
    objects.add("Dataset1");
    objects.add("Dataset2");
    return objects.iterator();
  }

  @Test(dataProvider = "dp")
  public void test(String dataset) {}
}
