package test.junit4;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class ClassRuleJUnit4Sample {

  @ClassRule
  public static ExternalResource resource =
      new ExternalResource() {
        @Override
        protected void before() throws Throwable {
          throw new IllegalArgumentException("before");
        }

        @Override
        protected void after() {
          throw new IllegalArgumentException("after");
        }
      };

  @Test
  public void myTest() {
    System.out.println("yay!");
  }
}
