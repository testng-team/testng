package org.testng.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.TestRunner;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.log.TextFormatter;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlClass;

/**
 * Helper methods to parse annotations.
 *
 * @author Cedric Beust, Apr 26, 2004
 */
public final class Utils {
  private static final String LINE_SEP = System.getProperty("line.separator");

  public static final char[] SPECIAL_CHARACTERS =
      {'*','/','\\','?','%',':',';','<','>','&','~','|'};
  public static final char CHAR_REPLACEMENT = '_';
  public static final char UNICODE_REPLACEMENT = 0xFFFD;

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

  public static XmlClass[] classesToXmlClasses(Class<?>[] classes) {
    List<XmlClass> result = Lists.newArrayList();

    for (Class<?> cls : classes) {
      result.add(new XmlClass(cls, true /* load classes */));
    }

    return result.toArray(new XmlClass[classes.length]);
  }

  public static String[] parseMultiLine(String line) {
    List<String> vResult = Lists.newArrayList();
    if (isStringNotBlank(line)) {
      StringTokenizer st = new StringTokenizer(line, " ");
      while (st.hasMoreTokens()) {
        vResult.add(st.nextToken());
      }
      // Bug in split when passed " " : returns one too many result
      //      result = line.split(" ");
    }

    return vResult.toArray(new String[vResult.size()]);
  }

  public static void writeUtf8File(@Nullable String outputDir, String fileName, XMLStringBuffer xsb, String prefix) {
    try {
      final File outDir = (outputDir != null) ? new File(outputDir) : new File("").getAbsoluteFile();
      if (!outDir.exists()) {
        outDir.mkdirs();
      }
      final File file = new File(outDir, fileName);
      if (!file.exists()) {
        file.createNewFile();
      }
      final OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
      if (prefix != null) {
        w.append(prefix);
      }
      xsb.toWriter(w);
      w.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Writes the content of the sb string to the file named filename in outDir encoding the output as UTF-8.
   * If outDir does not exist, it is created.
   *
   * @param outputDir the output directory (may not exist). If <tt>null</tt> then current directory is used.
   * @param fileName the filename
   * @param sb the file content
   */
  public static void writeUtf8File(@Nullable String outputDir, String fileName, String sb) {
    final String outDirPath= outputDir != null ? outputDir : "";
    final File outDir= new File(outDirPath);
    writeFile(outDir, fileName, escapeUnicode(sb), "UTF-8", false /* don't append */);
  }

  /**
   * Writes the content of the sb string to the file named filename in outDir. If
   * outDir does not exist, it is created.
   *
   * @param outputDir the output directory (may not exist). If <tt>null</tt> then current directory is used.
   * @param fileName the filename
   * @param sb the file content
   */
  public static void writeFile(@Nullable String outputDir, String fileName, String sb) {
    final String outDirPath= outputDir != null ? outputDir : "";
    final File outDir= new File(outDirPath);
    writeFile(outDir, fileName, sb, null, false /* don't append */);
  }

  /**
   * Writes the content of the sb string to the file named filename in outDir. If
   * outDir does not exist, it is created.
   *
   * @param outDir the output directory (may not exist). If <tt>null</tt> then current directory is used.
   * @param fileName the filename
   * @param sb the file content
   */
  private static void writeFile(@Nullable File outDir, String fileName, String sb, @Nullable String encoding, boolean append) {
    try {
      if (outDir == null) {
        outDir = new File("").getAbsoluteFile();
      }
      if (!outDir.exists()) {
        outDir.mkdirs();
      }

      fileName = replaceSpecialCharacters(fileName);
      File outputFile = new File(outDir, fileName);
      if (!append) {
        outputFile.delete();
        outputFile.createNewFile();
      }
      writeFile(outputFile, sb, encoding, append);
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

  private static void writeFile(File outputFile, String sb, @Nullable String encoding, boolean append) {
    BufferedWriter fw = null;
    try {
      fw = openWriter(outputFile, encoding, append);
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

  /**
   * Open a BufferedWriter for the specified file. If output directory doesn't
   * exist, it is created. If the output file exists, it is deleted. The output file is
   * created in any case.
   * @param outputDir output directory. If <tt>null</tt>, then current directory is used
   * @param fileName file name
   * @throws IOException if anything goes wrong while creating files.
   */
  public static BufferedWriter openWriter(@Nullable String outputDir, String fileName) throws IOException {
    String outDirPath= outputDir != null ? outputDir : "";
    File outDir= new File(outDirPath);
    if (outDir.exists()) {
      outDir.mkdirs();
    }
    fileName = replaceSpecialCharacters(fileName);
    File outputFile = new File(outDir, fileName);
    outputFile.delete();
    return openWriter(outputFile, null, false);
  }

  private static BufferedWriter openWriter(File outputFile, @Nullable String encoding, boolean append) throws IOException {
    if (!outputFile.exists()) {
      outputFile.createNewFile();
    }
    OutputStreamWriter osw= null;
    if (null != encoding) {
      osw = new OutputStreamWriter(new FileOutputStream(outputFile, append), encoding);
    }
    else {
      osw = new OutputStreamWriter(new FileOutputStream(outputFile, append));
    }
    return new BufferedWriter(osw);
  }

  private static void ppp(String s) {
    Utils.log("Utils", 0, s);
  }

  /**
   * @param result
   */
  public static void dumpMap(Map<?, ?> result) {
    System.out.println("vvvvv");
    for (Map.Entry<?, ?> entry : result.entrySet()) {
      System.out.println(entry.getKey() + " => " + entry.getValue());
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
    List<String> vResult = Lists.newArrayList();
    Class<?> cls = m.getDeclaringClass();

    // Collect groups on the class
    ITestAnnotation tc = AnnotationHelper.findTest(finder, cls);
    if (null != tc) {
      for (String group : tc.getDependsOnGroups()) {
        vResult.add(group);
      }
    }

    // Collect groups on the method
    ITestAnnotation tm = AnnotationHelper.findTest(finder, m);
    if (null != tm) {
      String[] groups = tm.getDependsOnGroups();

      //       ppp("Method:" + m + " #Groups:" + groups.length);
      for (String group : groups) {
        vResult.add(group);
      }
    }

    return vResult.toArray(new String[vResult.size()]);
  }

  /**
   * @return The list of groups this method belongs to, including the
   * class groups
   */
  public static String[] groupsForThisMethodForTest(Method m, IAnnotationFinder finder) {
    List<String> vResult = Lists.newArrayList();
    Class<?> cls = m.getDeclaringClass();

    // Collect groups on the class
    ITestAnnotation tc = AnnotationHelper.findTest(finder, cls);
    if (null != tc) {
      for (String group : tc.getGroups()) {
        vResult.add(group);
      }
    }

    // Collect groups on the method
    ITestAnnotation tm = AnnotationHelper.findTest(finder, m);
    if (null != tm) {
      String[] groups = tm.getGroups();

      //       ppp("Method:" + m + " #Groups:" + groups.length);
      for (String group : groups) {
        vResult.add(group);
      }
    }

    return vResult.toArray(new String[vResult.size()]);
  }

  /**
   * @return The list of groups this method belongs to, including the
   * class groups
   */
  public static String[] groupsForThisMethodForConfiguration(Method m, IAnnotationFinder finder) {
    String[] result = {};

    // Collect groups on the method
    ITestAnnotation tm = AnnotationHelper.findTest(finder, m);
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
    IConfigurationAnnotation tm = AnnotationHelper.findConfiguration(finder, m);
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
        System.out.println("[" + cls + "] " + msg);
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
//  public static int calculateInvokedMethodCount(IResultMap map) {
//    return calculateInvokedMethodCount(
//        (ITestNGMethod[]) map.getAllMethods().toArray(new ITestNGMethod[map.size()]));
//  }

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
    List<String> strings = Lists.newArrayList();

    while (idx != -1) {
      strings.add(string.substring(start, idx).trim());
      start = idx + len;
      idx = string.indexOf(sep, start);
    }

    strings.add(string.substring(start).trim());

    return strings.toArray(new String[strings.size()]);
  }

  public static void initLogger(Logger logger, String outputLogPath) {
    try {
      logger.setUseParentHandlers(false);
      FileHandler fh = new FileHandler(outputLogPath);
      fh.setFormatter(new TextFormatter());
      fh.setLevel(Level.INFO);
      logger.addHandler(fh);
    }
    catch (SecurityException | IOException se) {
      se.printStackTrace();
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
    InputStream inputStream = clasz.getResourceAsStream("/" + resourceName);
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

  public static String defaultIfStringEmpty(String s, String defaultValue) {
    return isStringEmpty(s) ? defaultValue : s;
  }

  public static boolean isStringBlank(String s) {
    return s == null || "".equals(s.trim());
  }

  public static boolean isStringEmpty(String s) {
    return s == null || "".equals(s);
  }

  public static boolean isStringNotBlank(String s) {
    return !isStringBlank(s);
  }

  public static boolean isStringNotEmpty(String s) {
    return !isStringEmpty(s);
  }

  /**
   * @return an array of two strings: the short stack trace and the long stack trace.
   */
  public static String[] stackTrace(Throwable t, boolean toHtml) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();

    String fullStackTrace = sw.getBuffer().toString();
    String shortStackTrace;

    if (Boolean.getBoolean(TestNG.SHOW_TESTNG_STACK_FRAMES) || TestRunner.getVerbose() >= 2) {
      shortStackTrace = fullStackTrace;
    } else {
      shortStackTrace = filterTrace(sw.getBuffer().toString());
    }

    if (toHtml) {
      shortStackTrace = escapeHtml(shortStackTrace);
      fullStackTrace = escapeHtml(fullStackTrace);
    }

    return new String[] {
        shortStackTrace, fullStackTrace
    };
  }

  private static final Map<Character, String> ESCAPES = new HashMap<Character, String>() {
    private static final long serialVersionUID = 1285607660247157523L;

  {
    put('<', "&lt;");
    put('>', "&gt;");
    put('\'', "&apos;");
    put('"', "&quot;");
    put('&', "&amp;");
  }};

  public static String escapeHtml(String s) {
    if (s == null) {
      return null;
    }

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      String nc = ESCAPES.get(c);
      if (nc != null) {
        result.append(nc);
      } else {
        result.append(c);
      }
    }

    return result.toString();
  }

  public static String escapeUnicode(String s) {
    if (s == null) {
      return null;
    }

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      char ca = (Character.isDefined(c)) ? c: UNICODE_REPLACEMENT;
      result.append(ca);
    }

    return result.toString();
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
          "reflect",
          "org.apache.maven.surefire"
      };

      int excludedCount = 0;
      while((line = bufferedReader.readLine()) != null) {
        boolean isExcluded = false;
        for (String excluded : excludedStrings) {
          if(line.contains(excluded)) {
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

  public static String toString(Object object, Class<?> objectClass) {
    if(null == object) {
      return "null";
    }
    final String toString= object.toString();
    if(isStringEmpty(toString)) {
      return "\"\"";
    }
    else if (String.class.equals(objectClass)) {
      return "\"" + toString + '\"';
    }
    else {
      return toString;
    }
  }

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

  public static String arrayToString(String[] strings) {
    StringBuffer result = new StringBuffer("");
    if ((strings != null) && (strings.length > 0)) {
      for (int i = 0; i < strings.length; i++) {
        result.append(strings[i]);
        if (i < strings.length - 1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }

  /**
   * If the file name contains special characters like *,/,\ and so on,
   * exception will be thrown and report file will not be created.<br>
   * Special characters are platform specific and they are not same for
   * example on Windows and Macintosh. * is not allowed on Windows, but it is on Macintosh.<br>
   * In order to have the same behavior of testng on the all platforms, characters like * will
   * be replaced on all platforms whether they are causing the problem or not.
   *
   * @param fileName file name that could contain special characters.
   * @return fileName with special characters replaced
   * @author Borojevic
   */
  public static String replaceSpecialCharacters(String fileName) {
   if (fileName == null || fileName.length() == 0) {
     return fileName;
   }
   for (char element : SPECIAL_CHARACTERS) {
     fileName = fileName.replace(element, CHAR_REPLACEMENT);
   }

   return fileName;
  }

  public static <T> String join(List<T> objects, String separator) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < objects.size(); i++) {
      if (i > 0) {
        result.append(separator);
      }
      result.append(objects.get(i).toString());
    }
    return result.toString();
  }

  public static void copyFile(File from, File to) {
    to.getParentFile().mkdirs();
    try (InputStream in = new FileInputStream(from); OutputStream out = new FileOutputStream(to)) {
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } catch(IOException e){
      e.printStackTrace();
    }
  }

  /**
   * @return a temporary file with the given content.
   */
  public static File createTempFile(String content) {
    try {
      // Create temp file.
      File result = File.createTempFile("testng-tmp", "");

      // Delete temp file when program exits.
      result.deleteOnExit();

      // Write to temp file
      BufferedWriter out = new BufferedWriter(new FileWriter(result));
      out.write(content);
      out.close();

      return result;
    } catch (IOException e) {
      throw new TestNGException(e);
    }
  }

  /**
   * Make sure that either we have an instance or if not, that the method is static
   */
  public static void checkInstanceOrStatic(Object instance, Method method) {
    if (instance == null && method != null && ! Modifier.isStatic(method.getModifiers())) {
      throw new TestNGException("Can't invoke " + method + ": either make it static or add "
          + "a no-args constructor to your class");
    }
  }

  public static void checkReturnType(Method method, Class<?>... returnTypes) {
    if (method == null) {
      return;
    }
    for (Class<?> returnType : returnTypes) {
      if (method.getReturnType() == returnType) {
        return;
      }
    }
    throw new TestNGException(method.getDeclaringClass().getName() + "."
              + method.getName() + " MUST return " + toString(returnTypes) + " but returns " + method.getReturnType().getName());
  }

  private static String toString(Class<?>[] classes) {
    StringBuilder sb = new StringBuilder("[ ");
    for (int i=0; i<classes.length;) {
      Class<?> clazz = classes[i];
      if (clazz.isArray()) {
        sb.append(clazz.getComponentType().getName()).append("[]");
      } else {
        sb.append(clazz.getName());
      }
      if (++i < classes.length) { // increment and compare
        sb.append(" or ");
      }
    }
    sb.append(" ]");
    return sb.toString();
  }

  /**
   * Returns the string representation of the specified object, transparently
   * handling null references and arrays.
   *
   * @param obj
   *            the object
   * @return the string representation
   */
  public static String toString(Object obj) {
    String result;
    if (obj != null) {
      if (obj instanceof boolean[]) {
        result = Arrays.toString((boolean[]) obj);
      } else if (obj instanceof byte[]) {
        result = Arrays.toString((byte[]) obj);
      } else if (obj instanceof char[]) {
        result = Arrays.toString((char[]) obj);
      } else if (obj instanceof double[]) {
        result = Arrays.toString((double[]) obj);
      } else if (obj instanceof float[]) {
        result = Arrays.toString((float[]) obj);
      } else if (obj instanceof int[]) {
        result = Arrays.toString((int[]) obj);
      } else if (obj instanceof long[]) {
        result = Arrays.toString((long[]) obj);
      } else if (obj instanceof Object[]) {
        result = Arrays.deepToString((Object[]) obj);
      } else if (obj instanceof short[]) {
        result = Arrays.toString((short[]) obj);
      } else {
        result = obj.toString();
      }
    } else {
      result = "null";
    }
    return result;
  }
}
