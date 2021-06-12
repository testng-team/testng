package test.inheritance;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

public class ZBase_0 {
  protected static List<String> m_methodList = new ArrayList<>();

  @BeforeTest
  public void beforeTest() {
    m_methodList = new ArrayList<>();
  }

  @BeforeMethod
  public void initApplication() {
    m_methodList.add("initApplication");
  }

  @AfterMethod
  public void tearDownApplication() {
    m_methodList.add("tearDownApplication");
  }

  public static List<String> getMethodList() {
    return m_methodList;
  }
}
