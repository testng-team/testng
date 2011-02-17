package test.guice;

import com.google.inject.Module;

import org.testng.IModuleFactory;
import org.testng.ITestContext;

public class ModuleFactory implements IModuleFactory {

  @Override
  public Module createModule(ITestContext context, Class<?> testClass) {
    String parameter = context.getCurrentXmlTest().getParameter("inject");
    String expected = "guice";
    if (! expected.equals(parameter)) {
      throw new RuntimeException("Excepted parameter to be " + expected + ", got " + parameter);
    }
    if (GuiceModuleFactoryTest.class == testClass) {
      return new GuiceExampleModule();
    } else {
      throw new RuntimeException("Don't know how to create a module for class " + testClass);
    }
  }

}
