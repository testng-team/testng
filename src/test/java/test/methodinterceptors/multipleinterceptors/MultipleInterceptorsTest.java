package test.methodinterceptors.multipleinterceptors;

import java.util.Collections;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class MultipleInterceptorsTest extends SimpleBaseTest {
    
    @Test
    public void testMultipleInterceptors(){
      TestNG tng = create(FooTest.class);
      tng.setMethodInterceptor(new FirstInterceptor());
      tng.setMethodInterceptor(new SecondInterceptor());
      tng.setMethodInterceptor(new ThirdInterceptor());
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      tng.run();
      Assert.assertEquals(tla.getPassedTests().size(), 1);
      Assert.assertEquals(tla.getPassedTests().get(0).getName(), "d");
    }

    @Test
    public void testMultipleInterceptorsWithPreserveOrder() {
      TestNG tng = create();
      tng.setTestSuites(Collections.singletonList(
          getPathToResource("/methodinterceptors/multipleinterceptors/multiple-interceptors.xml")));
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      tng.run();
      Assert.assertEquals(tla.getPassedTests().get(0).getMethod().getDescription(), "abc");
    }
}
