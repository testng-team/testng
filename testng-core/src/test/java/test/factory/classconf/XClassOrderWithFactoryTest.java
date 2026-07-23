package test.factory.classconf;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class XClassOrderWithFactoryTest {
  @Test
  public void testBeforeAfterClassInvocationsWithFactory() {
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class[] {XClassOrderWithFactory.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    assertThat(XClassOrderWithFactory.LOG.toString())
        .isEqualTo(XClassOrderWithFactory.EXPECTED_LOG);
  }
}
