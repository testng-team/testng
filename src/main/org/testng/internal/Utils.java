package org.testng.internal;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.*;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IConfiguration;
import org.testng.internal.annotations.IParameters;
import org.testng.internal.annotations.ITest;
import org.testng.log.TextFormatter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/**
 * Helper methods to parse annotations.
 *
 * @author Cedric Beust, Apr 26, 2004
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alexandru Popescu</a>
 */
public final class Utils {
  private static final String LINE_SEP = System.getProperty("line.separator");
  
  /**
   * Hide constructor for utility class.
   */
  private Utils() {
    // Hide constructor
  }
  
  /**
   * Splits the given String s into tokens where the separator is
   * either the space character or the comma character. For example, 
   * if s is "a,b, c" this method returns {"a", "b", "c"}
   *  
   * @param s the string to split
   * @return the split token 
   */  
  public static String[] stringToArray(String s) {
    // TODO CQ would s.split() be a better way of doing this?
    StringTokenizer st = new StringTokenizer(s, " ,");
    String[] result = new String[st.countTokens()];
    for (int i = 0; i < result.length; i++) {
      result[i] = st.nextToken();
    }

    return result;
  }

  public static Class[] xmlClassesToClasses(List<XmlClass> classes) {
    List<Class> result = new ArrayList<Class>();

    for (XmlClass xmlClass : classes) {
      result.add(xmlClass.getSupportClass());
    }

    Class[] xmlClasses = result.toArray(new Class[classes.size()]);
    Map<Class, Class> withNestedClasses = new HashMap<Class, Class>();
    findAllClasses(xmlClasses, withNestedClasses);

    return withNestedClasses.values().toArray(new Class[withNestedClasses.size()]);
  }

  public static XmlClass[] classesToXmlClasses(Class[] classes) {
    List<XmlClass> result = new ArrayList<XmlClass>();

    for (Class cls : classes) {
      result.add(new XmlClass(cls));
    }

    return result.toArray(new XmlClass[classes.length]);

  }
  
  /**
   * Find all the classes inside this array, including nested ones.
   * @param classes
   * @return
   */
  private static void findAllClasses(Class[] classes, Map<Class, Class> result) {
    for (Class cls : classes) {
      if (!result.containsKey(cls)) {
        result.put(cls, cls);
      }
    }
  }

  public static String[] parseMultiLine(String line) {
    List<String> vResult = new ArrayList<String>();
    if ((null != line) && !"".equals(line.trim())) {
      StringTokenizer st = new StringTokenizer(line, " ");
      while (st.hasMoreTokens()) {
        vResult.add(st.nextToken());
      }
      // Bug in split when passed " " : returns one too many result
      //      result = line.split(" ");
    }

    String[] result = vResult.toArray(new String[vResult.size()]);

    return result;
  }

  /**
   * Writes the content of the sb string buffer to the file named filename in outputDir. If 
   * outDir does not exist, it is created.
   *
   * @param outputDir the output directory (may not exist).
   * @param fileName the filename
   * @param sb the file content
   */
  public static void writeFile(String outputDir, String fileName, StringBuffer sb) {
    writeFile(outputDir, fileName, sb.toString());
  }

  /**
   * Writes the content of the sb string to the file named filename in outDir. If 
   * outDir does not exist, it is created.
   *
   * @param outputDir the output directory (may not exist). If <tt>null</tt> then current directory is used.
   * @param fileName the filename
   * @param sb the file content
   */
  public static void writeFile(String outputDir, String fileName, String sb) {
    try {
      final String outDirPath= outputDir != null ? outputDir : "";
      final File outDir= new File(outDirPath);
      if (!outDir.exists()) {
        outDir.mkdirs();
      }
      
      File outputFile = new File(outDir, fileName);
      outputFile.delete();
      outputFile.createNewFile();
      
      writeFile(outputFile, sb);
    }
    catch (IOException e) {
      if (TestRunner.getVerbose() > 1) {
        e.printStackTrace();
      }
      else {
        log("[Utils]", 1, e.getMessage());
      }
    }
  }

  private static void writeFile(File outputFile, String sb) {
    BufferedWriter fw = null;
    try {
      if (! outputFile.exists()) outputFile.createNewFile();
      fw = new BufferedWriter(new FileWriter(outputFile, false));
      fw.write(sb);
  
      Utils.log("", 3, "Creating " + outputFile.getAbsolutePath());
    }
    catch(IOException ex) {
      if (TestRunner.getVerbose() > 1) {
        System.err.println("ERROR WHILE WRITING TO " + outputFile);
        ex.printStackTrace();
      }
      else {
        log("[Utils]", 1, "Error while writing to " + outputFile + ": " + ex.getMessage());
      }
    }
    finally {
      try {
        if (fw != null) {
          fw.close();
        }
      }
      catch (IOException e) {
        ; // ignore
      }
    }
  }

  private static void ppp(String s) {
    Utils.log("Utils", 0, s);
  }

  /**
   * @param result
   */
  public static void dumpMap(Map result) {
    System.out.println("vvvvv");
    for (Iterator it = result.keySet().iterator(); it.hasNext();) {
      Object key = it.next();
      Object value = result.get(key);
      System.out.println(key + " => " + value);
    }
    System.out.println("^^^^^");
  }

  /**
   * @param allMethods
   */
  public static void dumpMethods(List<ITestNGMethod> allMethods) {
    ppp("======== METHODS:");
    for (ITestNGMethod tm : allMethods) {
      ppp("  " + tm);
    }
  }

  /**
   * @return The list of dependent groups for this method, including the
   * class groups
   */
  public static String[] dependentGroupsForThisMethodForTest(Method m, IAnnotationFinder finder) {
    List<String> vResult = new ArrayList<String>();
    Class cls = m.getDeclaringClass();

    // Collect groups on the class
    ITest tc = AnnotationHelper.findTest(finder, cls);
    if (null != tc) {
      for (String group : tc.getDependsOnGroups()) {
        vResult.add(group);
      }
    }

    // Collect groups on the method
    ITest tm = AnnotationHelper.findTest(finder, m);
    if (null != tm) {
      String[] groups = tm.getDependsOnGroups();

      //       ppp("Method:" + m + " #Groups:" + groups.length);
      for (String group : groups) {
        vResult.add(group);
      }
    }

    return (String[]) vResult.toArray(new String[vResult.size()]);
  }

  /**
   * @return The list of groups this method belongs to, including the
   * class groups
   */
  public static String[] groupsForThisMethodForTest(Method m, IAnnotationFinder finder) {
    List<String> vResult = new ArrayList<String>();
    Class cls = m.getDeclaringClass();

    // Collect groups on the class
    ITest tc = AnnotationHelper.findTest(finder, cls);
    if (null != tc) {
      for (String group : tc.getGroups()) {
        vResult.add(group);
      }
    }

    // Collect groups on the method
    ITest tm = AnnotationHelper.findTest(finder, m);
    if (null != tm) {
      String[] groups = tm.getGroups();

      //       ppp("Method:" + m + " #Groups:" + groups.length);
      for (String group : groups) {
        vResult.add(group);
      }
    }

    return (String[]) vResult.toArray(new String[vResult.size()]);
  }

  /**
   * @return The list of groups this method belongs to, including the
   * class groups
   */
  public static String[] groupsForThisMethodForConfiguration(Method m, IAnnotationFinder finder) {
    String[] result = {};

    // Collect groups on the method
    ITest tm = AnnotationHelper.findTest(finder, m);
    if (null != tm) {
      result = tm.getGroups();
    }

    return result;
  }

  /**
   * @return The list of groups this method depends on, including the
   * class groups
   */
  public static String[] dependentGroupsForThisMethodForConfiguration(Method m,
                                                                      IAnnotationFinder finder) {
    String[] result = {};

    // Collect groups on the method
    IConfiguration tm = AnnotationHelper.findConfiguration(finder, m);
    if (null != tm) {
      result = tm.getDependsOnGroups();
    }

    return result;
  }
  
  public static void log(String msg) {
    log("Utils", 2, msg);
  }

  /**
   * Logs the the message to System.out if level is greater than
   * or equal to TestRunner.getVerbose(). The message is logged as:
   * <pre>
   *     "[cls] msg"
   * </pre>
   *
   * @param cls the class name to prefix the log message.
   * @param level the logging level of the message.
   * @param msg the message to log to System.out.
   */
  public static void log(String cls, int level, String msg) {
    // Why this coupling on a static member of TestRunner.getVerbose()?
    if (TestRunner.getVerbose() >= level) {
      if (cls.length() > 0) {
        System.out.println('[' + cls + "] " + msg);
      }
      else {
        System.out.println(msg);
      }
    }
  }
  
  public static void error(String errorMessage) {
    System.err.println("[Error] " + errorMessage);
  }

  /**
   * @return The number of methods invoked, taking into account the number
   * of instances.
   */
  public static int calculateInvokedMethodCount(IResultMap map) {
    return calculateInvokedMethodCount(
        (ITestNGMethod[]) map.getAllMethods().toArray(new ITestNGMethod[map.size()]));
  }
  
  public static int calculateInvokedMethodCount(ITestNGMethod[] methods) {
    return methods.length;
//    int result = 0;
//
//    for (ITestNGMethod method : methods) {
//      int instanceCount = method.getInvocationCount();
//      result += instanceCount;
//    }
//
//    return result;
  }

//  public static int calculateInvokedMethodCount(Map<ITestNGMethod, ITestResult> methods) {
//    return calculateInvokedMethodCount(methods.keySet().toArray(new ITestNGMethod[methods.values()
//                                                                .size()]));
//  }

  /**
   * Create an instance for the given class.
   */
  public static Object createInstance(Class declaringClass,
                                      Map<Class, IClass> classes,
                                      XmlTest xmlTest,
                                      IAnnotationFinder finder,
                                      IObjectFactory objectFactory) {
    Object result;

    try {

      //
      // Any annotated constructor?
      //
      Constructor constructor = findAnnotatedConstructor(finder, declaringClass);
      if (null != constructor) {
        IParameters annotation = (IParameters) finder.findAnnotation(constructor,
                                                                     IParameters.class);

        String[] parameterNames = annotation.getValue();
        Object[] parameters = Parameters.createInstantiationParameters(constructor,
                                                          "@Parameters",
                                                          parameterNames,
                                                          xmlTest.getParameters(),
                                                          xmlTest.getSuite());
        result = objectFactory.newInstance(constructor, parameters);
      }

      //
      // No, just try to instantiate the parameterless constructor (or the one
      // with a String)
      //
      else {

        // If this class is a (non-static) nested class, the constructor contains a hidden
        // parameter of the type of the enclosing class
        Class[] parameterTypes = new Class[0];
        Object[] parameters = new Object[0];
        Class ec = getEnclosingClass(declaringClass);
        boolean isStatic = 0 != (declaringClass.getModifiers() & Modifier.STATIC);

        // Only add the extra parameter if the nested class is not static
        if ((null != ec) && !isStatic) {
          parameterTypes = new Class[] { ec };

          // Create an instance of the enclosing class so we can instantiate
          // the nested class (actually, we reuse the existing instance).
          IClass enclosingIClass = classes.get(ec);
          Object[] enclosingInstances;
          if (null != enclosingIClass) {
            enclosingInstances = enclosingIClass.getInstances(false);
            if ((null == enclosingInstances) || (enclosingInstances.length == 0)) {
              Object o = objectFactory.newInstance(ec.getConstructor(parameterTypes));
              enclosingIClass.addInstance(o);
              enclosingInstances = new Object[] { o };
            }
          }
          else {
            enclosingInstances = new Object[] { ec.newInstance() };
          }
          Object enclosingClassInstance = enclosingInstances[0];

          // Utils.createInstance(ec, classes, xmlTest, finder);
          parameters = new Object[] { enclosingClassInstance };
        } // isStatic
        Constructor ct = declaringClass.getDeclaredConstructor(parameterTypes);
        result = objectFactory.newInstance(ct, parameters);
      }
    }
    catch (TestNGException ex) {
      // We need to pass this along
      throw ex;
    }
    catch (NoSuchMethodException ex) {
      result = tryOtherConstructor(declaringClass);
    }
    catch (Throwable cause) {
      // Something else went wrong when running the constructor
      throw new TestNGException("An error occured while instantiating class " + declaringClass.getName(), cause);
    }

    return result;
  }

  /**
   * Class.getEnclosingClass() only exists on JDK5, so reimplementing it
   * here.
   */
  private static Class getEnclosingClass(Class declaringClass) {
    Class result = null;

    String className = declaringClass.getName();
    int index = className.indexOf("$");
    if (index != -1) {
      String ecn = className.substring(0, index);
      try {
        result = Class.forName(ecn);
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

  public static Object tryOtherConstructor(Class declaringClass) {
    Object result;
    try {
      Constructor ctor = declaringClass.getConstructor(new Class[] { String.class });
      result = ctor.newInstance(new Object[] { "Default test name" });
    }
    catch (Exception e) {
      String message = e.getMessage();
      if ((message == null) && (e.getCause() != null)) {
        message = e.getCause().getMessage();
      }
      String error = "Could not create an instance of class " + declaringClass
      + ((message != null) ? (": " + message) : "")
        + ".\nPlease make sure it has a constructor that accepts either a String or no parameter.";
      throw new TestNGException(error);
    }

    return result;
  }

  /**
   * Find the best constructor given the parameters found on the annotation
   */
  private static Constructor findAnnotatedConstructor(IAnnotationFinder finder,
                                                      Class declaringClass) {
    Constructor[] constructors = declaringClass.getDeclaredConstructors();

    for (int i = 0; i < constructors.length; i++) {
      Constructor result = constructors[i];
      IParameters annotation = (IParameters) finder.findAnnotation(result, IParameters.class);

      if (null != annotation) {
        String[] parameters = annotation.getValue();
        Class[] parameterTypes = result.getParameterTypes();
        if (parameters.length != parameterTypes.length) {
          throw new TestNGException("Parameter count mismatch:  " + result + "\naccepts "
                                    + parameterTypes.length
                                    + " parameters but the @Test annotation declares "
                                    + parameters.length);
        }
        else {
          return result;
        }
      }
    }

    return null;
  }

  /**
   * Tokenize the string using the separator.
   */
  public static String[] split(String string, String sep) {
    if ((string == null) || (string.length() == 0)) {
      return new String[0];
    }

    // TODO How different is this from: 
    // return string.split(sep);
    
    int start = 0;
    int idx = string.indexOf(sep, start);
    int len = sep.length();
    List strings = new ArrayList();

    while (idx != -1) {
      strings.add(string.substring(start, idx).trim());
      start = idx + len;
      idx = string.indexOf(sep, start);
    }

    strings.add(string.substring(start).trim());

    return (String[]) strings.toArray(new String[strings.size()]);
  }

  public static void initLogger(Logger logger, String outputLogPath) {
    try {
      logger.setUseParentHandlers(false);
      FileHandler fh = new FileHandler(outputLogPath);
      fh.setFormatter(new TextFormatter());
      fh.setLevel(Level.INFO);
      logger.addHandler(fh);
    }
    catch (SecurityException se) {
      se.printStackTrace();
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static void logInvocation(String reason, Method thisMethod, Object[] parameters) {
    String clsName = thisMethod.getDeclaringClass().getName();
    int n = clsName.lastIndexOf(".");
    if (n >= 0) {
      clsName = clsName.substring(n + 1);
    }
    String methodName = clsName + '.' + thisMethod.getName();
    if (TestRunner.getVerbose() >= 2) {
      StringBuffer paramString = new StringBuffer();
      if (parameters != null) {
        for (Object p : parameters) {
          paramString.append(p.toString()).append(' ');
        }
      }
      log("", 2, "Invoking " + reason + methodName + '(' + paramString + ')');
    }
  }

  public static void writeResourceToFile(File file, String resourceName, Class<?> clasz) throws IOException {
    InputStream inputStream = clasz.getResourceAsStream('/' + resourceName);
    if (inputStream == null) {
      System.err.println("Couldn't find resource on the class path: " + resourceName);
//      throw new IllegalArgumentException("Resource does not exist: " + resourceName);
    }
    
    else {
      
      try {
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
          int nread;
          byte[] buffer = new byte[4096];
          while (0 < (nread = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, nread);
          }
        } finally {
          outputStream.close();
        }
      } finally {
        inputStream.close();
      }
    }
  }

  public static boolean isStringEmpty(String s) {
    return s == null || "".equals(s);
  }

  public static String[] stackTrace(Throwable t, boolean tohtml) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();
    
    String fullStackTrace = sw.getBuffer().toString();
    String shortStackTrace;
    
    if (Boolean.getBoolean(TestNGCommandLineArgs.SHOW_TESTNG_STACK_FRAMES)
        || TestRunner.getVerbose() == -1) {
      shortStackTrace = fullStackTrace;
    }
    else {
      shortStackTrace = filterTrace(sw.getBuffer().toString());
    }
    
    if (tohtml) {
      shortStackTrace = shortStackTrace.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
      fullStackTrace = fullStackTrace.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
    
    return new String[] {
        shortStackTrace, fullStackTrace
    };
  }
  
  private static String filterTrace(String trace) {
    StringReader   stringReader = new StringReader(trace);
    BufferedReader bufferedReader = new BufferedReader(stringReader);
    StringBuffer buf = new StringBuffer();

    try {
      // first line contains the thrown exception
      String line = bufferedReader.readLine();
      if(line == null) {
        return "";
      }
      buf.append(line).append(LINE_SEP);
      
      //
      // the stack frames of the trace
      //
      String[] excludedStrings = new String[] {
          "org.testng",
          "reflect"
      };
      
      int excludedCount = 0;
      while((line = bufferedReader.readLine()) != null) {
        boolean isExcluded = false;
        for (String excluded : excludedStrings) {
          if(line.indexOf(excluded) != -1) {
            isExcluded = true;
            excludedCount++;
            break;
           }
        }
        if (! isExcluded) {
          buf.append(line).append(LINE_SEP);
        }
      }
      if (excludedCount > 0) {
        buf.append("... Removed " + excludedCount + " stack frames");
      }
    }
    catch(IOException ioex) {
      ; // do nothing
    }
    
    return buf.toString();
  }

  /**
   * @param object
   * @return
   */
  public static String toString(Object object, Class objectClass) {
    if(null == object) {
      return "null";
    }
    final String toString= object.toString();
    if("".equals(toString)) {
      return "\"\"";
    }
    else if (String.class.equals(objectClass)) {
      return '\"' + toString + '\"';
    }
    else {
      return toString;
    }
  }

  /**
   * @param method
   * @return
   */
  public static String detailedMethodName(ITestNGMethod method, boolean fqn) {
    StringBuffer buf= new StringBuffer();
    if(method.isBeforeSuiteConfiguration()) {
      buf.append("@BeforeSuite ");
    }
    else if(method.isBeforeTestConfiguration()) {
      buf.append("@BeforeTest ");
    }
    else if(method.isBeforeClassConfiguration()) {
      buf.append("@BeforeClass ");
    }
    else if(method.isBeforeGroupsConfiguration()) {
      buf.append("@BeforeGroups ");
    }
    else if(method.isBeforeMethodConfiguration()) {
      buf.append("@BeforeMethod ");
    }
    else if(method.isAfterMethodConfiguration()) {
      buf.append("@AfterMethod ");
    }
    else if(method.isAfterGroupsConfiguration()) {
      buf.append("@AfterGroups ");
    }
    else if(method.isAfterClassConfiguration()) {
      buf.append("@AfterClass ");
    }
    else if(method.isAfterTestConfiguration()) {
      buf.append("@AfterTest ");
    }
    else if(method.isAfterSuiteConfiguration()) {
      buf.append("@AfterSuite ");
    }
    
    return buf.append(fqn ? method.toString() : method.getMethodName()).toString(); 
  }
}
