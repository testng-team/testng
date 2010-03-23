package test.verify;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

/**
 * Illustrate the implementation of a @Verify/@Verifier test.
 * 
 * One method should be annotated with @Verifier and then each test method
 * annotationed with @Verify will be followed with a call to the @Verifier
 * method.
 */
public class VerifyTest {

  public static void main(String[] arg) {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { VerifyTest.class });
    IMethodInterceptor mi = new IMethodInterceptor() {

      public List<IMethodInstance> intercept(List<IMethodInstance> methods,
          ITestContext context) {
        List<IMethodInstance> result = Lists.newArrayList();
        IMethodInstance verifier = null;

        // Doing a na•ve approach here: we run through the list of methods
        // twice, once to find the verifier and once more to actually create
        // the result. Obviously, this can be done with just one loop
        for (IMethodInstance m : methods) {
          if (m.getMethod().getMethod().getAnnotation(Verifier.class) != null) {
            verifier = m;
            break;
          }
        }

        // Create the result with each @Verify method followed by a call
        // to the @Verifier method
        for (IMethodInstance m : methods) {
          if (m != verifier) {
            result.add(m);
          }

          if (m.getMethod().getMethod().getAnnotation(Verify.class) != null) {
            result.add(verifier);
          }
        }

        return result;
      }
      
    };
    tng.setMethodInterceptor(mi);
    tng.run();
  }

  @Verify
  @Test
  public void f1() {
    log("f1");  
  }

  @Verify
  @Test
  public void f2() {
    log("f2");  
  }

  @Verifier
  @Test
  public void verify() {
    log("Verifying");
  }

  private void log(String string) {
    System.out.println(string);
  }
}
