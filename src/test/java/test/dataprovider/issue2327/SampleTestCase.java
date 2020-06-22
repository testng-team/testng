package test.dataprovider.issue2327;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestCase {

  @BeforeMethod(alwaysRun = true)
  public void setup(Method method, Object[] params) {
    throw new RuntimeException("setup fail.");
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
