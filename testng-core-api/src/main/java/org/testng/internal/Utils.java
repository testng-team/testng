package org.testng.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.log4testng.Logger;
import org.testng.reporters.XMLStringBuffer;

/** Helper methods to parse annotations. */
public final class Utils {

  private static final String LINE_SEP = RuntimeBehavior.getDefaultLineSeparator();

  private static final char[] SPECIAL_CHARACTERS = {
    '*', '/', '\\', '?', '%', ':', ';', '<', '>', '&', '~', '|'
  };
  public static final char CHAR_REPLACEMENT = '_';
  public static final char UNICODE_REPLACEMENT = 0xFFFD;
  private static final String FORMAT = String.format("[%s]", Utils.class.getSimpleName());

  private static final Logger LOG = Logger.getLogger(Utils.class);

  private static final Map<Character, String> ESCAPES = new HashMap<>();

  static {
    ESCAPES.put('<', "&lt;");
    ESCAPES.put('>', "&gt;");
    ESCAPES.put('\'', "&apos;");
    ESCAPES.put('"', "&quot;");
    ESCAPES.put('&', "&amp;");
  }

  // Moved from TestRunner
  // TODO: replace with Logger?
  private static int m_verbose = 1;

  /** Hide constructor for utility class. */
  private Utils() {
    // Hide constructor
  }

  public static int getVerbose() {
    return m_verbose;
  }

  public static void setVerbose(int n) {
    m_verbose = n;
  }

  public static void writeUtf8File(
      @Nullable String outputDir, String fileName, XMLStringBuffer xsb, String prefix) {
    try {
      final File outDir =
          (outputDir != null) ? new File(outputDir) : new File("").getAbsoluteFile();
      if (!outDir.exists()) {
        outDir.mkdirs();
      }
      final File file = new File(outDir, fileName);
      if (!file.exists()) {
        file.createNewFile();
      }
      try (final OutputStreamWriter w =
          new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
        if (prefix != null) {
          w.append(prefix);
        }
        xsb.toWriter(w);
      }
    } catch (IOException ex) {
      LOG.error(ex.getMessage(), ex);
    }
  }

  /**
   * Writes the content of the sb string to the file named filename in outDir encoding the output as
   * UTF-8. If outDir does not exist, it is created.
   *
   * @param outputDir the output directory (may not exist). If <code>null</code> then current
   *     directory is used.
   * @param fileName the filename
   * @param sb the file content
   */
  public static void writeUtf8File(@Nullable String outputDir, String fileName, String sb) {
    final String outDirPath = outputDir != null ? outputDir : "";
    final File outDir = new File(outDirPath);
    writeFile(outDir, fileName, escapeUnicode(sb), "UTF-8");
  }

  /**
   * Writes the content of the sb string to the file named filename in outDir. If outDir does not
   * exist, it is created.
   *
   * @param outputDir the output directory (may not exist). If <code>null</code> then current
   *     directory is used.
   * @param fileName the filename
   * @param sb the file content
   */
  public static void writeFile(@Nullable String outputDir, String fileName, String sb) {
    final String outDirPath = outputDir != null ? outputDir : "";
    final File outDir = new File(outDirPath);
    writeFile(outDir, fileName, sb, null);
  }

  /**
   * Writes the content of the sb string to the file named filename in outDir. If outDir does not
   * exist, it is created.
   *
   * @param outputFolder the output directory (may not exist). If <tt>null</tt> then current
   *     directory is used.
   * @param fileNameParameter the filename
   * @param sb the file content
   */
  private static void writeFile(
      @Nullable File outputFolder, String fileNameParameter, String sb, @Nullable String encoding) {
    File outDir = outputFolder;
    String fileName = fileNameParameter;
    try {
      if (outDir == null) {
        outDir = new File("").getAbsoluteFile();
      }
      if (!outDir.exists()) {
        outDir.mkdirs();
      }

      fileName = replaceSpecialCharacters(fileName);
      File outputFile = new File(outDir, fileName);
      outputFile.delete();
      log(FORMAT, 3, "Attempting to create " + outputFile);
      log(FORMAT, 3, "  Directory " + outDir + " exists: " + outDir.exists());
      outputFile.createNewFile();
      writeFile(outputFile, sb, encoding);
    } catch (IOException e) {
      if (getVerbose() > 1) {
        LOG.error(e.getMessage(), e);
      } else {
        log(FORMAT, 1, e.getMessage());
      }
    }
  }

  private static void writeFile(File outputFile, String sb, @Nullable String encoding) {
    try (BufferedWriter fw = openWriter(outputFile, encoding)) {
      fw.write(sb);

      Utils.log("", 3, "Creating " + outputFile.getAbsolutePath());
    } catch (IOException ex) {
      if (getVerbose() > 1) {
        LOG.error("ERROR WHILE WRITING TO " + outputFile, ex);
      } else {
        log(FORMAT, 1, "Error while writing to " + outputFile + ": " + ex.getMessage());
      }
    }
    // ignore
  }

  /**
   * Open a BufferedWriter for the specified file. If output directory doesn't exist, it is created.
   * If the output file exists, it is deleted. The output file is created in any case.
   *
   * @param outputDir output directory. If <code>null</code>, then current directory is used
   * @param fileNameParameter file name
   * @throws IOException if anything goes wrong while creating files.
   */
  public static BufferedWriter openWriter(@Nullable String outputDir, String fileNameParameter)
      throws IOException {
    String fileName = fileNameParameter;
    String outDirPath = outputDir != null ? outputDir : "";
    File outDir = new File(outDirPath);
    if (!outDir.exists()) {
      outDir.mkdirs();
    }
    fileName = replaceSpecialCharacters(fileName);
    File outputFile = new File(outDir, fileName);
    outputFile.delete();
    return openWriter(outputFile, null);
  }

  private static BufferedWriter openWriter(File outputFile, @Nullable String encoding)
      throws IOException {
    if (!outputFile.exists()) {
      outputFile.createNewFile();
    }
    OutputStreamWriter osw;
    if (null != encoding) {
      osw = new OutputStreamWriter(new FileOutputStream(outputFile), encoding);
    } else {
      osw = new OutputStreamWriter(new FileOutputStream(outputFile));
    }
    return new BufferedWriter(osw);
  }

  public static void log(String msg) {
    log("Utils", 2, msg);
  }

  /**
   * Logs the the message to System.out if level is greater than or equal to getVerbose(). The
   * message is logged as:
   *
   * <pre>
   *     "[cls] msg"
   * </pre>
   *
   * @param cls the class name to prefix the log message.
   * @param level the logging level of the message.
   * @param msg the message to log to System.out.
   */
  public static void log(String cls, int level, String msg) {
    // Why this coupling on a static member of getVerbose()?
    if (getVerbose() >= level) {
      if (cls.length() > 0) {
        LOG.info("[" + cls + "] " + msg);
      } else {
        LOG.info(msg);
      }
    }
  }

  public static void error(String errorMessage) {
    LOG.error("[Error] " + errorMessage);
  }

  public static void warn(String warnMsg) {
    LOG.warn(warnMsg);
  }

  /* Tokenize the string using the separator. */
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

    return strings.toArray(new String[0]);
  }

  public static void writeResourceToFile(File file, String resourceName, Class<?> clasz)
      throws IOException {
    InputStream inputStream = clasz.getResourceAsStream("/" + resourceName);
    if (inputStream == null) {
      LOG.error("Couldn't find resource on the class path: " + resourceName);
      return;
    }
    try {
      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        int nread;
        byte[] buffer = new byte[4096];
        while (0 < (nread = inputStream.read(buffer))) {
          outputStream.write(buffer, 0, nread);
        }
      }
    } finally {
      inputStream.close();
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
   * Helper that returns a short stack trace.
   *
   * @param t - The {@link Throwable} exception
   * @param toHtml - <code>true</code> if the stacktrace should be translated to html as well
   * @return - A string that represents the short stack trace.
   */
  public static String longStackTrace(Throwable t, boolean toHtml) {
    return buildStackTrace(t, toHtml, StackTraceType.FULL);
  }

  /**
   * Helper that returns a long stack trace.
   *
   * @param t - The {@link Throwable} exception
   * @param toHtml - <code>true</code> if the stacktrace should be translated to html as well
   * @return - A string that represents the full stack trace.
   */
  public static String shortStackTrace(Throwable t, boolean toHtml) {
    return buildStackTrace(t, toHtml, StackTraceType.SHORT);
  }

  private static String buildStackTrace(Throwable t, boolean toHtml, StackTraceType type) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();
    String stackTrace = sw.getBuffer().toString();
    if (type == StackTraceType.SHORT && !isTooVerbose()) {
      stackTrace = filterTrace(sw.getBuffer().toString());
    }
    if (toHtml) {
      stackTrace = escapeHtml(stackTrace);
    }
    return stackTrace;
  }

  private static boolean isTooVerbose() {
    return RuntimeBehavior.showTestNGStackFrames() || getVerbose() >= 2;
  }

  private enum StackTraceType {
    SHORT,
    FULL
  }

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
      char ca = Character.isDefined(c) ? c : UNICODE_REPLACEMENT;
      result.append(ca);
    }

    return result.toString();
  }

  static String filterTrace(String trace) {
    StringReader stringReader = new StringReader(trace);
    BufferedReader bufferedReader = new BufferedReader(stringReader);
    StringBuilder buf = new StringBuilder();

    try {
      // first line contains the thrown exception
      String line = bufferedReader.readLine();
      if (line == null) {
        return "";
      }
      buf.append(line).append(LINE_SEP);

      //
      // the stack frames of the trace
      //
      String[] excludedStrings =
          new String[] {"org.testng", "reflect", "org.gradle", "org.apache.maven.surefire"};

      int excludedCount = 0;
      while ((line = bufferedReader.readLine()) != null) {
        boolean isExcluded = false;
        for (String excluded : excludedStrings) {
          if (line.contains(excluded)) {
            isExcluded = true;
            excludedCount++;
            break;
          }
        }
        if (!isExcluded) {
          buf.append(line).append(LINE_SEP);
        }
      }
      if (excludedCount > 0) {
        buf.append("... Removed ").append(excludedCount).append(" stack frames");
      }
    } catch (IOException ioex) {
      // do nothing
    }

    return buf.toString();
  }

  public static String toString(Object object, Class<?> objectClass) {
    if (null == object) {
      return "null";
    }
    final String toString = toString(object);
    if (isStringEmpty(toString)) {
      return "\"\"";
    } else if (String.class.equals(objectClass)) {
      return "\"" + toString + '\"';
    } else {
      return toString;
    }
  }

  public static String detailedMethodName(ITestNGMethod method, boolean fqn) {
    String tempName = annotationFormFor(method);
    if (!tempName.isEmpty()) {
      tempName += " ";
    }
    return tempName + (fqn ? method.toString() : method.getMethodName());
  }

  /**
   * Given a TestNG method, returns the corresponding annotation based on the method type
   *
   * @param method - An {@link ITestNGMethod} object.
   * @return - A String representation of the corresponding annotation.
   */
  public static String annotationFormFor(ITestNGMethod method) {
    if (method.isBeforeSuiteConfiguration()) {
      return "@BeforeSuite";
    }
    if (method.isBeforeTestConfiguration()) {
      return "@BeforeTest";
    }
    if (method.isBeforeClassConfiguration()) {
      return "@BeforeClass";
    }
    if (method.isBeforeGroupsConfiguration()) {
      return "@BeforeGroups";
    }
    if (method.isBeforeMethodConfiguration()) {
      return "@BeforeMethod";
    }
    if (method.isAfterMethodConfiguration()) {
      return "@AfterMethod";
    }
    if (method.isAfterGroupsConfiguration()) {
      return "@AfterGroups";
    }
    if (method.isAfterClassConfiguration()) {
      return "@AfterClass";
    }
    if (method.isAfterTestConfiguration()) {
      return "@AfterTest";
    }
    if (method.isAfterSuiteConfiguration()) {
      return "@AfterSuite";
    }
    return "";
  }

  public static String arrayToString(String[] strings) {
    return String.join(", ", strings);
  }

  /**
   * If the file name contains special characters like *,/,\ and so on, exception will be thrown and
   * report file will not be created.<br>
   * Special characters are platform specific and they are not same for example on Windows and
   * Macintosh. * is not allowed on Windows, but it is on Macintosh.<br>
   * In order to have the same behavior of testng on the all platforms, characters like * will be
   * replaced on all platforms whether they are causing the problem or not.
   *
   * @param fileNameParameter file name that could contain special characters.
   * @return fileName with special characters replaced
   */
  public static String replaceSpecialCharacters(String fileNameParameter) {
    String fileName = fileNameParameter;
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

  /* Make sure that either we have an instance or if not, that the method is static */
  public static void checkInstanceOrStatic(Object instance, Method method) {
    if (instance == null && method != null && !Modifier.isStatic(method.getModifiers())) {
      throw new TestNGException(
          "Can't invoke "
              + method
              + ": either make it static or add "
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
    throw new TestNGException(
        method.getDeclaringClass().getName()
            + "."
            + method.getName()
            + " MUST return "
            + toString(returnTypes)
            + " but returns "
            + method.getReturnType().getName());
  }

  private static String toString(Class<?>[] classes) {
    StringBuilder sb = new StringBuilder("[ ");
    for (int i = 0; i < classes.length; i++) {
      Class<?> clazz = classes[i];
      if (clazz.isArray()) {
        sb.append(clazz.getComponentType().getName()).append("[]");
      } else {
        sb.append(clazz.getName());
      }
      if ((i + 1) < classes.length) { // increment and compare
        sb.append(" or ");
      }
    }
    sb.append(" ]");
    return sb.toString();
  }

  /**
   * Returns the string representation of the specified object, transparently handling null
   * references and arrays.
   *
   * @param obj the object
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

  public static String stringifyTypes(Class<?>[] parameterTypes) {
    return Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(","));
  }
}
