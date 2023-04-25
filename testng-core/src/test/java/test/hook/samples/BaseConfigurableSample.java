package test.hook.samples;

import java.lang.reflect.Method;
import org.testng.IConfigurable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public abstract class BaseConfigurableSample implements IConfigurable {

  @BeforeSuite
  public void bs() {}

  @BeforeTest
  public void bt() {}

  @BeforeMethod
  public void bm(Method m) {}

  @BeforeClass
  public void bc() {}
}
