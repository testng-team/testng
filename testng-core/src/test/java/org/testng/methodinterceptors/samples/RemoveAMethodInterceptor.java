package test.methodinterceptors;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

/** An interceptor that removes the method called "two". */
public class RemoveAMethodInterceptor implements IMethodInterceptor {

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    List<IMethodInstance> result = new ArrayList<>();

    for (IMethodInstance method : methods) {
      String name = method.getMethod().getMethodName();
      if (!name.equals("two")) {
        result.add(method);
      }
    }

    log(this, methods, result);

    return result;
  }

  public static void log(
      IMethodInterceptor listener, List<IMethodInstance> input, List<IMethodInstance> output) {
    String msg =
        listener.getClass().getName()
            + " - Input:"
            + getMethodNames(input)
            + " "
            + input.size()
            + " methods."
            + " Output:"
            + getMethodNames(output)
            + " "
            + output.size()
            + " methods";
    System.err.println(msg);
  }

  public static List<String> getMethodNames(List<IMethodInstance> methods) {
    List<String> names = new ArrayList<>();
    for (IMethodInstance m : methods) {
      names.add(m.getMethod().getMethodName());
    }
    return names;
  }
}
