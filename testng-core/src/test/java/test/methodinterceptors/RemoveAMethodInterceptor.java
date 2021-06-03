package test.methodinterceptors;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.List;

/**
 * An interceptor that removes the method called "two".
 */
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

  public static void log(IMethodInterceptor listener, List<IMethodInstance> input,
      List<IMethodInstance> output) {
    StringBuilder msg = new StringBuilder().append(listener.getClass().getName())
        .append(" - Input:").append(getMethodNames(input)).append(" ").append(input.size())
        .append(" methods.").append(" Output:").append(getMethodNames(output)).append(" ")
        .append(output.size()).append(" methods");
    log(msg.toString());
  }

  public static List<String> getMethodNames(List<IMethodInstance> methods) {
    List<String> names = new ArrayList<>();
    for (IMethodInstance m : methods) {
      names.add(m.getMethod().getMethodName());
    }
    return names;
  }

  private static void log(String s) {
//    System.out.println("[MI2] " + s);
  }
}