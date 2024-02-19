package test.dataprovider.issue2327;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {

  @BeforeMethod(alwaysRun = true)
  public void setup(Method method, Object[] params) {
    throw new RuntimeException("setup fail.");
  }

  @DataProvider(name = "dp", parallel = true)
  public Iterator<Object> dp() {
    List<Object> objects = new ArrayList<>();
    objects.add("Dataset1");
    objects.add("Dataset2");
    return objects.iterator();
  }

  @Test(dataProvider = "dp")
  public void test(String dataset) {}
}
