package test.priority;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.List;


public class BaseSample {
  public static List<String> m_methods = Lists.newArrayList();

  protected void add(String m) {
    String s = m;
//    System.out.println("BaseSample recording " + this + " " + s);
    synchronized(m_methods) {
      m_methods.add(s);
    }
  }

  @BeforeClass
  public void bc() {
    m_methods = Lists.newArrayList();
  }

  @Test
  public void f1() { add("f1"); }

  @Test
  public void f2() { add("f2"); }

  @Test
  public void f3() { add("f3"); }

  @Test
  public void f4() { add("f4"); }

  @Test
  public void f5() { add("f5"); }

  @Test
  public void f6() { add("f6"); }

  @Test
  public void f7() { add("f7"); }

  @Test
  public void f8() { add("f8"); }
}
