package test.methodinterceptors.multipleinterceptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class MultipleInterceptorsTest extends SimpleBaseTest {
    
    public static List<Class> interceptors = new ArrayList<>();

    @Test
    public void testMultipleInterceptors(){
      TestNG tng = create();
      tng.setTestClasses(new Class[] { FooTest.class });
      tng.setMethodInterceptor(new ThirdInterceptor());
      tng.setMethodInterceptor(new FirstInterceptor());
      tng.setMethodInterceptor(new SecondInterceptor());
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      tng.run();
      Assert.assertEquals(tla.getPassedTests().size(), 1);
      Assert.assertEquals(tla.getPassedTests().get(0).getName(), "d");
      reset();
    }

    @Test
    public void testMultipleInterceptorsWithPreserveOrder() {
      TestNG tng = create();
      tng.setTestSuites(Arrays.asList("target/test-classes/methodinterceptors/multipleinterceptors/multiple-interceptors.xml"));
      tng.run();
      //Assert.assertEquals(interceptors.size(), 3);
      Assert.assertTrue(interceptors.get(0).equals(FirstInterceptor.class));
      Assert.assertTrue(interceptors.get(1).equals(SecondInterceptor.class));
      Assert.assertTrue(interceptors.get(2).equals(ThirdInterceptor.class));
      reset();
    }
    
    private void reset(){
        interceptors.clear();
    }
}
