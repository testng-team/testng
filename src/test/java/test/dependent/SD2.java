package test.dependent;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class SD2 {
  public static List<String> m_log = new ArrayList<>();

  @Test(groups = { "one" })
  public void oneA() {
    m_log.add("oneA");
  }

  @Test
  public void canBeRunAnytime() {
    m_log.add("canBeRunAnytime");
  }

  @Test(dependsOnGroups = { "one" } )
  public void secondA() {
    m_log.add("secondA");
  }

  @Test(dependsOnMethods= { "secondA" })
  public void thirdA() {
    m_log.add("thirdA");
  }

  @Test(groups = { "one" })
  public void oneB() {
    m_log.add("oneB");
  }

}