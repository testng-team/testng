package test.guice.issue279;

import com.google.inject.Module;
import org.testng.IModuleFactory;
import org.testng.ITestContext;

public class TestDIFactory implements IModuleFactory {

  @Override
  public Module createModule(ITestContext context, Class<?> testClass) {
    return new SampleModule();
  }
}
