package org.testng.internal.reflect;

import org.testng.TestNGException;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Thrown from MethodMatcher.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class MethodMatcherException extends TestNGException {

  public MethodMatcherException(final String message, final Method method, final Object[] args) {
    this(generateMessage(message, method, args));
  }

  public MethodMatcherException(String message) {
    super(message);
  }

  public MethodMatcherException(String message, Throwable cause) {
    super(message, cause);
  }

  public MethodMatcherException(Throwable cause) {
    super(cause);
  }

  private static String generateMessage(final String message, final Method method, final Object[] args) {
    final StringBuilder sb = new StringBuilder();
    sb.append(message).append("\n").append("Method: ");
    if (method != null) {
      final Parameter[] parameter = ReflectionRecipes.getMethodParameters(method);
      sb.append(method.getName()).append("(").append(Arrays.toString(parameter)).append(")");
    } else {
      sb.append("null");
    }
    sb.append("\n").append("Arguments: ");
    if (args != null) {
      sb.append("[");
      for (int i = 0; i < args.length; i++) {
        final Object arg = args[i];
        if (arg != null) {
          sb.append("(").append(arg.getClass().getName()).append(")").append(arg);
        } else {
          sb.append("null");
        }
        if (i < args.length - 1) {
          sb.append(",");
        }
      }
      sb.append("]");
    } else {
      sb.append("Arguments: null");
    }
    return sb.toString();
  }
}
