package test.github1490;

import org.testng.annotations.DataProvider;
import org.testng.internal.ConstructorOrMethod;

public class DataProviderHouse {

  @DataProvider(name = "cookie-master")
  public static Object[][] cookies(ConstructorOrMethod method) {
    TestInfo value;
    if (method.getConstructor() != null) {
      value = (TestInfo) method.getConstructor().getAnnotation(TestInfo.class);
    } else {
      value = method.getMethod().getAnnotation(TestInfo.class);
    }
    String name = value.name();
    if ("glutton".equalsIgnoreCase(name)) {
      return new Object[][] {{"oreo", 200}};
    }
    if ("nibbler".equalsIgnoreCase(name)) {
      return new Object[][] {{"marie-gold", 10}};
    }
    return new Object[][] {
      {"oreo", 200},
      {"marie-gold", 10}
    };
  }
}
