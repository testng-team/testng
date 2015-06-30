package test.methodinterceptors.multipleinterceptors;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.Collections;

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
    // FIXME With or without preserve-order, test is working
    public void testMultipleInterceptorsWithPreserveOrder() {
      TestNG tng = create();
      tng.setTestSuites(Collections.singletonList("target/test-classes/methodinterceptors/multipleinterceptors/multiple-interceptors.xml"));
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      tng.run();
      Assert.assertEquals(tla.getPassedTests().get(0).getMethod().getDescription(), "abcabc"); // Interceptor are called twice => GITHUB #154
    }
}
