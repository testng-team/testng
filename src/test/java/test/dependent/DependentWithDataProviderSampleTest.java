package test.dependent;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

public class DependentWithDataProviderSampleTest
{
  public static List<String> m_log = Lists.newArrayList();
  private String param;

  @Factory( dataProvider = "prov" )
  public DependentWithDataProviderSampleTest( String param )
  {
    this.param = param;
  }

  @DataProvider( name = "prov" )
  public static Object[][] dataProvider()
  {
    return new Object[][] {
      { "One" },
      { "Two" },
      { "Three" },
    };
  }

  private void log(String s) {
    m_log.add(s + "#" + param);
  }

  @BeforeClass
  public void prepare()
  {
    log("prepare");
  }

  @Test
  public void test1()
  {
    log("test1");
  }

  @Test( dependsOnMethods = "test1" )
  public void test2()
  {
    log("test2");
  }

  @AfterClass
  public void clean()
  {
    log("clean");
  }

  @Override
  public String toString() {
    return "[" + param + "]";
  }
}
