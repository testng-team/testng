package test.configuration.github1700;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.collections.Lists;

public class BaseClassSample {

  public static List<String> messages = Lists.newArrayList();

  @BeforeMethod(alwaysRun = true)
  public void setUp(Method method) {
    if (method.getName().endsWith("test1")) {
      assertThat(2).isEqualTo(1);
    }
    messages.add(getClass().getCanonicalName() + ".setup()");
  }
}
