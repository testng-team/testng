package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.testng.TestNGException;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.reflect.ReflectionHelper;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/** Utility class for different class manipulations. */
public final class ClassHelper {

  /** The additional class loaders to find classes in. */
  private static final List<ClassLoader> classLoaders = new Vector<>();

  private static final String CLASS_HELPER = ClassHelper.class.getSimpleName();

  /**
   * When given a file name to form a class name, the file name is parsed and divided into segments.
   * For example, "c:/java/classes/com/foo/A.class" would be divided into 6 segments {"C:" "java",
   * "classes", "com", "foo", "A"}. The first segment actually making up the class name is [3]. This
   * value is saved in lastGoodRootIndex so that when we parse the next file name, we will try 3
   * right away. If 3 fails we will take the long approach. This is just a optimization cache value.
   */
  private static int lastGoodRootIndex = -1;

  /** Hide constructor. */
  private ClassHelper() {
    // Hide Constructor
  }

  /* Add a class loader to the searchable loaders. */
  public static void addClassLoader(final ClassLoader loader) {
    classLoaders.add(loader);
  }

  static List<ClassLoader> appendContextualClassLoaders(List<ClassLoader> currentLoaders) {
    List<ClassLoader> allClassLoaders = Lists.newArrayList();
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      allClassLoaders.add(contextClassLoader);
    }
    allClassLoaders.addAll(currentLoaders);
    return allClassLoaders;
  }

  /**
   * Tries to load the specified class using the context ClassLoader or if none, than from the
   * default ClassLoader. This method differs from the standard class loading methods in that it
   * does not throw an exception if the class is not found but returns null instead.
   *
   * @param className the class name to be loaded.
   * @return the class or null if the class is not found.
   */
  public static Class<?> forName(final String className) {
    List<ClassLoader> allClassLoaders = appendContextualClassLoaders(classLoaders);

    for (ClassLoader classLoader : allClassLoaders) {
      if (null == classLoader) {
        continue;
      }
      try {
        return classLoader.loadClass(className);
      } catch (ClassNotFoundException | NoClassDefFoundError ex) {
        // With additional class loaders, it is legitimate to ignore ClassNotFoundException /
        // NoClassDefFoundError
        if (classLoaders.isEmpty()) {
          logClassNotFoundError(className, ex);
        }
      }
    }
    if (RuntimeBehavior.shouldSkipUsingCallerClassLoader()) {
      return null;
    }

    try {
      return Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      logClassNotFoundError(className, cnfe);
      return null;
    }
  }

  private static void logClassNotFoundError(String className, Throwable ex) {
    Utils.log(
        CLASS_HELPER,
        2,
        "Could not instantiate " + className + " : Class doesn't exist (" + ex.getMessage() + ")");
  }

  /**
   * For the given class, returns the method annotated with &#64;Factory or null if none is found.
   * This method does not search up the superclass hierarchy. If more than one method is @Factory
   * annotated, a TestNGException is thrown.
   *
   * @param cls The class to search for the @Factory annotation.
   * @param finder The finder (JDK 1.4 or JDK 5.0+) use to search for the annotation.
   * @return the @Factory <CODE>methods</CODE>
   */
  // Code smell: The javadocs for this method says that if more than one Factory method is found
  // an exception is thrown. But the method is not doing that. This needs more investigation for
  // regression side effects before being addressed.
  public static List<ConstructorOrMethod> findDeclaredFactoryMethods(
      Class<?> cls, IAnnotationFinder finder) {
    List<ConstructorOrMethod> result = new ArrayList<>();
    BiConsumer<IFactoryAnnotation, Executable> consumer =
        (f, executable) -> {
          if (f != null) {
            ConstructorOrMethod factory = new ConstructorOrMethod(executable);
            factory.setEnabled(f.getEnabled());
            result.add(factory);
          }
        };

    for (Method method : getAvailableMethods(cls)) {
      IFactoryAnnotation f = finder.findAnnotation(method, IFactoryAnnotation.class);
      consumer.accept(f, method);
    }

    for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
      IFactoryAnnotation f = finder.findAnnotation(constructor, IFactoryAnnotation.class);
      consumer.accept(f, constructor);
    }

    return result;
  }

  /**
   * @param clazz - The {@link Class} in which the search is to be done.
   * @return - A {@link Set} of {@link Method} excluding default methods from base class interfaces.
   */
  public static Set<Method> getAvailableMethodsExcludingDefaults(Class<?> clazz) {
    // First group the methods based on names
    Map<String, List<Method>> groups =
        getAvailableMethods(clazz).stream().collect(Collectors.groupingBy(Method::getName));
    // Remove any methods that are default methods (which is usually true only with methods in
    // interfaces that are classified as "default" methods
    groups.values().stream()
        .filter(group -> group.size() > 1)
        .forEach(group -> group.removeIf(Method::isDefault));
    // Wrap them back into a set and return to the caller.
    return groups.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
  }

  /*
   * Extract all callable methods of a class and all its super (keeping in mind the Java access
   * rules).
   */
  public static Set<Method> getAvailableMethods(Class<?> clazz) {
    if (clazz == null || clazz.equals(Object.class)) {
      return Sets.newHashSet();
    }
    Map<String, Set<Method>> methods = Maps.newHashMap();
    for (final Method declaredMethod : ReflectionHelper.getLocalMethods(clazz)) {
      appendMethod(methods, declaredMethod);
    }

    Class<?> parent = clazz.getSuperclass();
    if (null != parent) {
      while (!Object.class.equals(parent)) {
        Set<Map.Entry<String, Set<Method>>> extractedMethods =
            extractMethods(clazz, parent, methods).entrySet();
        for (Map.Entry<String, Set<Method>> extractedMethod : extractedMethods) {
          Set<Method> m = methods.get(extractedMethod.getKey());
          if (m == null) {
            methods.put(extractedMethod.getKey(), extractedMethod.getValue());
          } else {
            m.addAll(extractedMethod.getValue());
          }
        }
        parent = parent.getSuperclass();
      }
    }

    Set<Method> returnValue = Sets.newHashSet();
    for (Set<Method> each : methods.values()) {
      returnValue.addAll(each);
    }
    return returnValue;
  }

  private static void appendMethod(Map<String, Set<Method>> methods, Method declaredMethod) {
    Set<Method> declaredMethods =
        methods.computeIfAbsent(declaredMethod.getName(), k -> Sets.newHashSet());
    declaredMethods.add(declaredMethod);
  }

  private static Map<String, Set<Method>> extractMethods(
      Class<?> childClass, Class<?> clazz, Map<String, Set<Method>> collected) {
    Map<String, Set<Method>> methods = Maps.newHashMap();

    Method[] declaredMethods = clazz.getDeclaredMethods();

    Package childPackage = childClass.getPackage();
    Package classPackage = clazz.getPackage();
    boolean isSamePackage = isSamePackage(childPackage, classPackage);

    for (Method method : declaredMethods) {
      if (canInclude(isSamePackage, method, collected)) {
        appendMethod(methods, method);
      }
    }

    return methods;
  }

  private static boolean canInclude(
      boolean isSamePackage, Method method, Map<String, Set<Method>> collected) {
    int methodModifiers = method.getModifiers();
    boolean visible =
        (Modifier.isPublic(methodModifiers) || Modifier.isProtected(methodModifiers))
            || (isSamePackage && !Modifier.isPrivate(methodModifiers));
    boolean hasNoInheritanceTraits =
        !isOverridden(method, collected) && !Modifier.isAbstract(methodModifiers);
    return visible && hasNoInheritanceTraits;
  }

  private static boolean isSamePackage(Package childPackage, Package classPackage) {
    boolean isSamePackage = false;

    if ((null == childPackage) && (null == classPackage)) {
      isSamePackage = true;
    }
    if ((null != childPackage) && (null != classPackage)) {
      isSamePackage = childPackage.getName().equals(classPackage.getName());
    }
    return isSamePackage;
  }

  private static boolean isOverridden(Method method, Map<String, Set<Method>> methodsByName) {
    Set<Method> collectedMethods = methodsByName.get(method.getName());
    if (collectedMethods == null) {
      return false;
    }
    Class<?> methodClass = method.getDeclaringClass();
    Class<?>[] methodParams = method.getParameterTypes();

    for (Method m : collectedMethods) {
      Class<?>[] paramTypes = m.getParameterTypes();
      if (methodClass.isAssignableFrom(m.getDeclaringClass())
          && methodParams.length == paramTypes.length) {
        boolean sameParameters = true;
        for (int i = 0; i < methodParams.length; i++) {
          if (!methodParams[i].equals(paramTypes[i])) {
            sameParameters = false;
            break;
          }
        }

        if (sameParameters) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Returns the Class object corresponding to the given name. The name may be of the following
   * form:
   *
   * <ul>
   *   <li>A class name: "org.testng.TestNG"
   *   <li>A class file name: "/testng/src/org/testng/TestNG.class"
   *   <li>A class source name: "d:\testng\src\org\testng\TestNG.java"
   * </ul>
   *
   * @param file the class name.
   * @return the class corresponding to the name specified.
   */
  public static Class<?> fileToClass(String file) {
    Class<?> result = null;

    if (!file.endsWith(".class") && !file.endsWith(".java")) {
      // Doesn't end in .java or .class, assume it's a class name

      if (file.startsWith("class ")) {
        file = file.substring("class ".length());
      }

      result = ClassHelper.forName(file);

      if (null == result) {
        throw new TestNGException("Cannot load class from file: " + file);
      }

      return result;
    }

    int classIndex = file.lastIndexOf(".class");
    if (-1 == classIndex) {
      classIndex = file.lastIndexOf(".java");
    }

    // Transforms the file name into a class name.

    // Remove the ".class" or ".java" extension.
    String shortFileName = file.substring(0, classIndex);

    // Split file name into segments. For example "c:/java/classes/com/foo/A"
    String[] segments = shortFileName.split("[/\\\\]", -1);

    //
    // Check if the last good root index works for this one. For example, if the previous
    // name was "c:/java/classes/com/foo/A.class" then lastGoodRootIndex is 3 and we
    // try to make a class name ignoring the first lastGoodRootIndex segments (3). This
    // will succeed rapidly if the path is the same as the one from the previous name.
    //
    if (-1 != lastGoodRootIndex) {

      StringBuilder className = new StringBuilder(segments[lastGoodRootIndex]);
      for (int i = lastGoodRootIndex + 1; i < segments.length; i++) {
        className.append(".").append(segments[i]);
      }

      result = ClassHelper.forName(className.toString());

      if (null != result) {
        return result;
      }
    }

    //
    // We haven't found a good root yet, start by resolving the class from the end segment
    // and work our way up.  For example, if we start with "c:/java/classes/com/foo/A"
    // we'll start by resolving "A", then "foo.A", then "com.foo.A" until something
    // resolves.  When it does, we remember the path we are at as "lastGoodRoodIndex".
    //

    String className = "";
    for (int i = segments.length - 1; i >= 0; i--) {
      if (className.length() == 0) {
        className = segments[i];
      } else {
        className = segments[i] + "." + className;
      }

      result = ClassHelper.forName(className);

      if (null != result) {
        lastGoodRootIndex = i;
        break;
      }
    }

    if (null == result) {
      throw new TestNGException("Cannot load class from file: " + file);
    }

    return result;
  }

  /**
   * @param cls - The class to look for.
   * @param suite - The {@link XmlSuite} whose &lt;test&gt; tags needs to be searched in.
   * @return - All the {@link XmlClass} objects that share the same &lt;test&gt; tag as the class.
   */
  public static XmlClass[] findClassesInSameTest(Class<?> cls, XmlSuite suite) {
    Collection<XmlClass> vResult = Sets.newHashSet();
    for (XmlTest test : suite.getTests()) {
      vResult.addAll(findClassesInSameTest(cls, test));
    }

    return vResult.toArray(new XmlClass[0]);
  }

  private static Collection<XmlClass> findClassesInSameTest(Class<?> cls, XmlTest xmlTest) {
    Collection<XmlClass> vResult = Sets.newHashSet();
    String className = cls.getName();
    for (XmlClass testClass : xmlTest.getXmlClasses()) {
      if (testClass.getName().equals(className)) {
        // Found it, add all the classes in this test in the result
        vResult.addAll(xmlTest.getXmlClasses());
        break;
      }
    }

    return vResult;
  }
}
