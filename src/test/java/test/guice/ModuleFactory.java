package test.guice;

import com.google.inject.Module;

import org.testng.IModuleFactory;

public class ModuleFactory implements IModuleFactory {

  @Override
  public Class<? extends Module> createModule(Class<?> testClass) {
    if (GuiceModuleFactoryTest.class == testClass) {
      return GuiceExampleModule.class;
    } else {
      throw new RuntimeException("Don't know how to create a module for class " + testClass);
    }
  }

}
