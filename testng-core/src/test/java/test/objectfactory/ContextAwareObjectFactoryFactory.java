package test.objectfactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.objects.ObjectFactoryImpl;

public class ContextAwareObjectFactoryFactory {

  public static int invoked;

  @ObjectFactory
  public ITestObjectFactory create(ITestContext context) {
    assertThat(context).isNotNull();
    invoked++;
    return new ObjectFactoryImpl();
  }
}
