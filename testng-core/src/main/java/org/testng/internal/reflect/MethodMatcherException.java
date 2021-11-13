package org.testng.internal.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.testng.TestNGException;

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

  static String generateMessage(
      final String message, final Constructor constructor, final Object[] args) {
    Parameter[] parameter = null;
    String name = null;
    if (constructor != null) {
      parameter = ReflectionRecipes.getConstructorParameters(constructor);
      name = constructor.getName();
    }
    return generateMessage(message, name, "Constructor", parameter, args);
  }

  public static String generateMessage(
      final String message, final Method method, final Object[] args) {
    Parameter[] parameter = null;
    String name = null;
    if (method != null) {
      parameter = ReflectionRecipes.getMethodParameters(method);
      name = method.getName();
    }
    return generateMessage(message, name, "Method", parameter, args);
  }

  private static String generateMessage(
      final String message,
      final String name,
      final String prefix,
      Parameter[] parameter,
      final Object[] args) {
    final StringBuilder sb = new StringBuilder();
    sb.append(message).append("\n").append(prefix).append(": ");
    if (name != null) {
      sb.append(name).append("(").append(Arrays.toString(parameter)).append(")");
    } else {
      sb.append("null");
    }
    sb.append("\n").append("Arguments: ");
    if (args != null) {
      sb.append("[");
      for (int i = 0; i < args.length; i++) {
        final Object arg = args[i];
        if (arg != null) {
          sb.append("(").append(arg.getClass().getName()).append(") ").append(stringify(arg));
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

  private static String stringify(Object object) {
    if (object.getClass().isArray()) {
      return Arrays.toString((Object[]) object);
    } else {
      return object.toString();
    }
  }
}
