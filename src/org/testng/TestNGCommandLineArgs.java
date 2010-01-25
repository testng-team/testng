package org.testng;


import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.internal.ClassHelper;
import org.testng.internal.Utils;
import org.testng.internal.version.VersionInfo;
import org.testng.log4testng.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TestNG/RemoteTestNG command line arguments parser.
 * 
 * @author Cedric Beust
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alexandru Popescu</a>
 */
public final class TestNGCommandLineArgs {
  /** This class's log4testng Logger. */
  private static final Logger LOGGER = Logger.getLogger(TestNGCommandLineArgs.class);
  
  public static final String SHOW_TESTNG_STACK_FRAMES = "testng.show.stack.frames";
  public static final String TEST_CLASSPATH = "testng.test.classpath";
  
  // These next two are used by the Eclipse plug-in
  public static final String PORT_COMMAND_OPT = "-port";
  public static final String HOST_COMMAND_OPT = "-host";
  
  /** The logging level option. */
  public static final String LOG = "-log";
  
  /** @deprecated replaced by DEFAULT_ANNOTATIONS_COMMAND_OPT. */
  public static final String TARGET_COMMAND_OPT = "-target";

  /** The default annotations option (useful in TestNG 15 only). */
  public static final String ANNOTATIONS_COMMAND_OPT = "-annotations";
  /** The test report output directory option. */
  public static final String OUTDIR_COMMAND_OPT = "-d";
  public static final String EXCLUDED_GROUPS_COMMAND_OPT = "-excludegroups";
  public static final String GROUPS_COMMAND_OPT = "-groups";
  public static final String JUNIT_DEF_OPT = "-junit";
  public static final String LISTENER_COMMAND_OPT = "-listener";
  public static final String MASTER_OPT = "-master";
  public static final String OBJECT_FACTORY_COMMAND_OPT = "-objectfactory";
  /**
   * Used to pass a reporter configuration in the form
   * <code>-reporter <reporter_name_or_class>:option=value[,option=value]</code>
   */
  public static final String REPORTER = "-reporter";
  /**
   * Used as map key for the complete list of report listeners provided with the above argument
   */
  public static final String REPORTERS_LIST = "-reporterslist";
  public static final String PARALLEL_MODE = "-parallel";
  public static final String SKIP_FAILED_INVOCATION_COUNT_OPT = "-skipfailedinvocationcounts";
  public static final String SLAVE_OPT = "-slave";
  /** The source directory option (when using JavaDoc type annotations). */
  public static final String SRC_COMMAND_OPT = "-sourcedir";
  public static final String SUITE_NAME_OPT = "-suitename";
  /** The list of test classes option. */
  public static final String TESTCLASS_COMMAND_OPT = "-testclass";
  public static final String TESTJAR_COMMAND_OPT = "-testjar";
  public static final String TEST_NAME_OPT = "-testname";
  public static final String TESTRUNNER_FACTORY_COMMAND_OPT = "-testrunfactory";
  public static final String THREAD_COUNT = "-threadcount";
  public static final String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount";
  public static final String USE_DEFAULT_LISTENERS = "-usedefaultlisteners";
  public static final String SUITE_DEF_OPT = "testng.suite.definitions";


  /** 
   * When given a file name to form a class name, the file name is parsed and divided 
   * into segments. For example, "c:/java/classes/com/foo/A.class" would be divided
   * into 6 segments {"C:" "java", "classes", "com", "foo", "A"}. The first segment 
   * actually making up the class name is [3]. This value is saved in m_lastGoodRootIndex
   * so that when we parse the next file name, we will try 3 right away. If 3 fails we
   * will take the long approach. This is just a optimization cache value.
   */  
  private static int m_lastGoodRootIndex = -1;
  
  /**
   * Hide the constructor for utility class.
   */
  private TestNGCommandLineArgs() {
    // Hide constructor for utility class
  }
  
  /**
   * Parses the command line options and returns a map from option string to parsed values. 
   * For example, if argv contains {..., "-sourcedir", "src/main", "-target", ...} then
   * the map would contain an entry in which the key would be the "-sourcedir" String and
   * the value would be the "src/main" String.
   *
   * @param originalArgv the command line options.
   * @return the parsed parameters as a map from option string to parsed values. 
   */
  public static Map parseCommandLine(final String[] originalArgv) {
    for (int i = 0; i < originalArgv.length; ++i) {
      LOGGER.debug("originalArgv[" + i + "] = \"" + originalArgv[i] + "\"");
    }
    // TODO CQ In this method, is this OK to simply ignore invalid parameters?
    LOGGER.debug("TestNG version: \"" + (VersionInfo.IS_JDK14 ? "14" : "15") + "\"");
    
    Map<String, Object> arguments = Maps.newHashMap();
    String[] argv = expandArgv(originalArgv);

    for (int i = 0; i < argv.length; i++) {
      if (OUTDIR_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(OUTDIR_COMMAND_OPT, argv[i + 1].trim());
        }
        else {
          LOGGER.error("WARNING: missing output directory after -d.  ignored");
        }
        i++;
      }
      else if (GROUPS_COMMAND_OPT.equalsIgnoreCase(argv[i]) 
          || EXCLUDED_GROUPS_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          String option = null;
          if (argv[i + 1].startsWith("\"")) {
            if (argv[i + 1].endsWith("\"")) {
              option = argv[i + 1].substring(1, argv[i + 1].length() - 1);
            }
            else {
              LOGGER.error("WARNING: groups option is not well quoted:" + argv[i + 1]);
              option = argv[i + 1].substring(1);
            }
          }
          else {
            option = argv[i + 1];
          }

          String opt = GROUPS_COMMAND_OPT.equalsIgnoreCase(argv[i]) 
            ? GROUPS_COMMAND_OPT : EXCLUDED_GROUPS_COMMAND_OPT;
          arguments.put(opt, option);
        }
        else {
          LOGGER.error("WARNING: missing groups parameter after -groups. ignored");
        }
        i++;
      }
      else if (LOG.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(LOG, Integer.valueOf(argv[i + 1].trim()));
        }
        else {
          LOGGER.error("WARNING: missing log level after -log.  ignored");
        }
        i++;
      }
      else if (JUNIT_DEF_OPT.equalsIgnoreCase(argv[i])) {
        arguments.put(JUNIT_DEF_OPT, Boolean.TRUE);
      }
      else if (TARGET_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(ANNOTATIONS_COMMAND_OPT, AnnotationTypeEnum.valueOf(argv[i + 1]));
          LOGGER.warn("The usage of " + TARGET_COMMAND_OPT + " has been deprecated. Please use " + ANNOTATIONS_COMMAND_OPT + " instead.");
          ++i;
        }
      }
      else if (ANNOTATIONS_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(ANNOTATIONS_COMMAND_OPT, AnnotationTypeEnum.valueOf(argv[i + 1]));
          ++i;
        }
      }
      else if (TESTRUNNER_FACTORY_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(TESTRUNNER_FACTORY_COMMAND_OPT, fileToClass(argv[++i]));
        }
        else {
          LOGGER.error("WARNING: missing ITestRunnerFactory class or file argument after "
              + TESTRUNNER_FACTORY_COMMAND_OPT);
        }
      }
      else if (OBJECT_FACTORY_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          Class<?> cls = fileToClass(argv[++i]);
          arguments.put(OBJECT_FACTORY_COMMAND_OPT, cls);
        }
        else {
          LOGGER.error("WARNING: missing IObjectFactory class/file list argument after "
              + OBJECT_FACTORY_COMMAND_OPT);
        }
      }
      else if (LISTENER_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          String strClass = argv[++i];
          String sep = ";";
          if (strClass.indexOf(",") >= 0) {
            sep = ",";
          }
          String[] strs = Utils.split(strClass, sep);
          List<Class<?>> classes = Lists.newArrayList();

          for (String cls : strs) {
            classes.add(fileToClass(cls));
          }

          arguments.put(LISTENER_COMMAND_OPT, classes);
        }
        else {
          LOGGER.error("WARNING: missing ITestListener class/file list argument after "
              + LISTENER_COMMAND_OPT);
        }
      }
      else if (TESTCLASS_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          while ((i + 1) < argv.length) {
            String nextArg = argv[i + 1].trim();
            if (!nextArg.toLowerCase().endsWith(".xml") && !nextArg.startsWith("-")) {

              // Assume it's a class name
              List<Class<?>> l = (List<Class<?>>) arguments.get(TESTCLASS_COMMAND_OPT);
              if (null == l) {
                l = Lists.newArrayList();
                arguments.put(TESTCLASS_COMMAND_OPT, l);
              }
              Class<?> cls = fileToClass(nextArg);
              if (null != cls) {
                l.add(cls);
              }

              i++;
            } // if
            else {
              break;
            }
          }
        }
        else {
          TestNG.exitWithError("-testclass must be followed by a classname");
        }
      }
      else if (TESTJAR_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(TESTJAR_COMMAND_OPT, argv[i + 1].trim());
        }
        else {
          TestNG.exitWithError("-testjar must be followed by a valid jar");
        }
        i++;
      }
      else if (SRC_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(SRC_COMMAND_OPT, argv[i + 1].trim());
        }
        else {
          TestNG.exitWithError(SRC_COMMAND_OPT + " must be followed by a directory path");
        }
        i++;
      }
      else if (HOST_COMMAND_OPT.equals(argv[i])) {
        String hostAddress = "127.0.0.1";
        if ((i + 1) < argv.length) {
          hostAddress = argv[i + 1].trim();
          i++;
        }
        else {
          LOGGER.warn("WARNING: "
              + HOST_COMMAND_OPT
              + " option should be followed by the host address. "
              + "Using default localhost.");
        }

        arguments.put(HOST_COMMAND_OPT, hostAddress);

      }
      else if (PORT_COMMAND_OPT.equals(argv[i])) {
        String portNumber = null;
        if ((i + 1) < argv.length) {
          portNumber = argv[i + 1].trim();
        }
        else {
          TestNG.exitWithError(
              PORT_COMMAND_OPT + " option should be followed by a valid port number.");
        }

        arguments.put(PORT_COMMAND_OPT, portNumber);
        i++;
      }
      else if (SLAVE_OPT.equals(argv[i])) {
        String propertiesFile = null;
        if ((i + 1) < argv.length) {
          propertiesFile = argv[i + 1].trim();
        }
        else {
          TestNG.exitWithError(SLAVE_OPT + " option should be followed by a valid file path.");
        }
        
        arguments.put(SLAVE_OPT, propertiesFile);
        i++;
      }
      else if (MASTER_OPT.equals(argv[i])) {
        String propertiesFile = null;
        if ((i + 1) < argv.length) {
      	  propertiesFile = argv[i + 1].trim();
        }
        else {
          TestNG.exitWithError(MASTER_OPT + " option should be followed by a valid file path.");
        }

        arguments.put(MASTER_OPT, propertiesFile);
        i++;
      }
      else if (PARALLEL_MODE.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(PARALLEL_MODE, argv[i + 1]);
          i++;
        }
      }
      else if (THREAD_COUNT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(THREAD_COUNT, argv[i + 1]);
          i++;
        }
      }
      else if (DATA_PROVIDER_THREAD_COUNT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(DATA_PROVIDER_THREAD_COUNT, argv[i + 1]);
           i++;
        }
      }
      else if (USE_DEFAULT_LISTENERS.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(USE_DEFAULT_LISTENERS, argv[i + 1]);
          i++;
        }
      }
      else if (SUITE_NAME_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(SUITE_NAME_OPT, trim(argv[i + 1]));
          i++;
        }
      }
      else if (TEST_NAME_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(TEST_NAME_OPT, trim(argv[i + 1]));
          i++;
        }
      }
      else if (REPORTER.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          ReporterConfig reporterConfig = ReporterConfig.deserialize(trim(argv[i + 1]));
          if (arguments.get(REPORTERS_LIST) == null) {
            arguments.put(REPORTERS_LIST, Lists.newArrayList());
          }
          ((List<ReporterConfig>)arguments.get(REPORTERS_LIST)).add(reporterConfig);
          i++;
        }
      }
      else if (SKIP_FAILED_INVOCATION_COUNT_OPT.equalsIgnoreCase(argv[i])) {
        arguments.put(SKIP_FAILED_INVOCATION_COUNT_OPT, Boolean.TRUE);
      }
      //
      // Unknown option
      //
      else if (argv[i].startsWith("-")) {
        TestNG.exitWithError("Unknown option: " + argv[i]);
      }      
      //
      // The XML files
      //
      // Read parameters just once, to get all xml files
      else if( arguments.get(SUITE_DEF_OPT) == null ){
        List<String> suiteDefs = Lists.newArrayList();

        // Iterates over all declared XML file params
        for (int k = i; k < argv.length; k++) {
          String file = argv[k].trim();
          if (file.toLowerCase().endsWith(".xml")) {
            suiteDefs.add(file);
          }
        }

        arguments.put(SUITE_DEF_OPT, suiteDefs);
      }
    }

    for (Map.Entry entry : arguments.entrySet()) {
      LOGGER.debug("parseCommandLine argument: \"" 
        + entry.getKey() + "\" = \"" + entry.getValue() + "\"");
    }
    return arguments;
  }


  /**
   * @param string
   * @return
   */
  private static String trim(String string) {
	String trimSpaces=string.trim();
	if (trimSpaces.startsWith("\"")) {
		if (trimSpaces.endsWith("\"")) {
			return trimSpaces.substring(1, trimSpaces.length() - 1);
		} else {
			return trimSpaces.substring(1);
		}
	} else {
		return trimSpaces;
	}
	
}

  /**
   * Expand the command line parameters to take @ parameters into account.
   * When @ is encountered, the content of the file that follows is inserted
   * in the command line
   * @param originalArgv the original command line parameters
   * @return the new and enriched command line parameters
   */
  private static String[] expandArgv(String[] originalArgv) {
    List<String> vResult = Lists.newArrayList();
    
    for (String arg : originalArgv) {

      if (arg.startsWith("@")) {
        String fileName = arg.substring(1);
        vResult.addAll(readFile(fileName));
      }
      else {
        vResult.add(arg);
      }
    }
    
    return vResult.toArray(new String[vResult.size()]);
  }   
   
   
//  /**
//   * Break a line of parameters into individual parameters as the command line parsing 
//   * would do. The line is assumed to contain only un-escaped double quotes. For example 
//   * the following Java string:
//   * " a    \"command\"\"line\" \"with quotes\"  a command line\" with quotes \"here there"
//   * would yield the following 7 tokens:
//   * a,commandline,with quotes,a,command,line with quotes here,there
//   * @param line the command line parameter to be parsed
//   * @return the list of individual command line tokens
//   */
//  private static List<String> parseArgs(String line) {
//    LOGGER.debug("parseArgs line: \"" + line + "\"");
//    final String SPACE = " ";
//    final String DOUBLE_QUOTE = "\"";
//    
//    // If line contains no double quotes, the space character is the only
//    // separator. Easy to do return quickly (logic is also easier to follow)
//    if (line.indexOf(DOUBLE_QUOTE) == -1) {
//      List<String> results = Arrays.asList(line.split(SPACE));
//      for (String result : results) {
//        LOGGER.debug("parseArgs result: \"" + result + "\"");
//      }
//      return results;
//    }
//    
//    // TODO There must be an easier way to do this with a regular expression.
//    
//    StringTokenizer st = new StringTokenizer(line, SPACE + DOUBLE_QUOTE, true);
//    List<String> results = Lists.newArrayList();
//    
//    /* 
//     * isInDoubleQuote toggles from false to true when we reach a double
//     * quoted string and toggles back to false when we exit. We need to
//     * know if we are in a double quoted string to treat blanks as normal
//     * characters. Out of quotes blanks separate arguments. 
//     * 
//     * The following example shows these toggle points:
//     * 
//     * " a    \"command\"\"line\" \"with quotes\"  a command line\" with quotes \"here there"
//     *        T        F T     F  T            F                 T              F
//     *
//     * If the double quotes are not evenly matched, an exception is thrown.
//     */
//    boolean isInDoubleQuote = false;
//
//    /*
//     * isInArg toggles from false to true when we enter a command line argument
//     * and toggles back to false when we exit. The logic is that we toggle to
//     * true at the first non-whitespace character met. We toggle back to false
//     * at first whitespace character not in double quotes or at end of line. 
//     * 
//     * The following example shows these toggle points:
//     * 
//     * " a    \"command\"\"line\" \"with quotes\"  a command line\" with quotes \"here there"
//     *   TF   T                F  T             F  TFT      FT                             F
//     */
//    boolean isInArg = false;
//
//    /* arg is a string buffer to create the argument by concatenating all tokens
//     * that compose it.
//     * 
//     * The following example shows the token returned by the parser and the 
//     * (spaces, double quotes, others) and resultant argument:
//     * 
//     * Input (argument):
//     * "line\" with quotes \"here"
//     * 
//     * Tokens (9):
//     * line,", ,with, ,quote, ,",here
//     */
//    StringBuffer arg = new StringBuffer();
//    
//    while (st.hasMoreTokens()) {
//      String token = st.nextToken();
//      
//      if (token.equals(SPACE)) {
//        if (isInArg) {
//          if (isInDoubleQuote) {
//            // Spaces within double quotes are treated as normal spaces
//            arg.append(SPACE);
//          }
//          else {
//            // First spaces outside double quotes marks the end of the argument. 
//            isInArg = false;
//            results.add(arg.toString());
//            arg = new StringBuffer();
//          }
//        }
//      }
//      else if (token.equals(DOUBLE_QUOTE)) {
//        // If we encounter a double quote, we may be entering a new argument 
//        // (isInArg is false) or continuing the current argument (isInArg is true).
//        isInArg = true;
//        isInDoubleQuote = !isInDoubleQuote;
//      }
//      else {
//        // We we encounter a new token, we may be entering a new argument 
//        // (isInArg is false) or continuing the current argument (isInArg is true).
//        isInArg = true;
//        arg.append(token);
//      }
//    }
//    
//    // In some (most) cases we exit this parsing because there are no tokens left
//    // but we have not encountered a token to indicate that the last argument has
//    // completely been read. For example, if the command line ends with a whitespace
//    // the isInArg will toggle to false and the argument will be completely read.
//    if (isInArg) {
//      // End of last argument
//      results.add(arg.toString());
//    }
//    
//    // If we exit the parsing of the command line with an uneven number of double 
//    // quotes, throw an exception.
//    if (isInDoubleQuote) {
//      throw new IllegalArgumentException("Unbalanced double quotes: \"" + line + "\"");
//    }
//    
//    for (String result : results) {
//      LOGGER.debug("parseArgs result: \"" + result + "\"");
//    }
//    
//    return results;
//  }

  /**
   * Reads the file specified by filename and returns the file content as a string.
   * End of lines are replaced by a space
   * 
   * @param fileName the command line filename
   * @return the file content as a string.
   */
  public static List<String> readFile(String fileName) {
    List<String> result = Lists.newArrayList();

    try {
      BufferedReader bufRead = new BufferedReader(new FileReader(fileName));

      String line;

      // Read through file one line at time. Print line # and line
      while ((line = bufRead.readLine()) != null) {
        result.add(line);
      }

      bufRead.close();
    }
    catch (IOException e) {
      LOGGER.error("IO exception reading command line file", e);
    }

    return result;

  }

  /**
   * Returns the Class object corresponding to the given name. The name may be
   * of the following form:
   * <ul>
   * <li>A class name: "org.testng.TestNG"</li>
   * <li>A class file name: "/testng/src/org/testng/TestNG.class"</li>
   * <li>A class source name: "d:\testng\src\org\testng\TestNG.java"</li>
   * </ul>
   * 
   * @param file
   *          the class name.
   * @return the class corresponding to the name specified.
   */
  private static Class<?> fileToClass(String file) {
    Class<?> result = null;
    
    if(!file.endsWith(".class") && !file.endsWith(".java")) {
      // Doesn't end in .java or .class, assume it's a class name
      result = ClassHelper.forName(file);

      if (null == result) {
        throw new TestNGException("Cannot load class from file: " + file);
      }

      return result;
    }
    
    int classIndex = file.lastIndexOf(".class");
    if (-1 == classIndex) {
      classIndex = file.lastIndexOf(".java");
//
//      if(-1 == classIndex) {
//        result = ClassHelper.forName(file);
//  
//        if (null == result) {
//          throw new TestNGException("Cannot load class from file: " + file);
//        }
//  
//        return result;
//      }
//
    }

    // Transforms the file name into a class name.
    
    // Remove the ".class" or ".java" extension.
    String shortFileName = file.substring(0, classIndex);
    
    // Split file name into segments. For example "c:/java/classes/com/foo/A"
    // becomes {"c:", "java", "classes", "com", "foo", "A"}
    String[] segments = shortFileName.split("[/\\\\]", -1);

    //
    // Check if the last good root index works for this one. For example, if the previous
    // name was "c:/java/classes/com/foo/A.class" then m_lastGoodRootIndex is 3 and we
    // try to make a class name ignoring the first m_lastGoodRootIndex segments (3). This 
    // will succeed rapidly if the path is the same as the one from the previous name.   
    //    
    if (-1 != m_lastGoodRootIndex) {
      
      // TODO use a SringBuffer here
      String className = segments[m_lastGoodRootIndex];
      for (int i = m_lastGoodRootIndex + 1; i < segments.length; i++) {
        className += "." + segments[i];
      }

      result = ClassHelper.forName(className);

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
    
    // TODO CQ use a StringBuffer here 
    String className = null;
    for (int i = segments.length - 1; i >= 0; i--) {
      if (null == className) {
        className = segments[i];
      }
      else {
        className = segments[i] + "." + className;
      }

      result = ClassHelper.forName(className);

      if (null != result) {
        m_lastGoodRootIndex = i;
        break;
      }
    }

    if (null == result) {
      throw new TestNGException("Cannot load class from file: " + file);
    }

    return result;
  }

//  private static void ppp(Object msg) {
//    System.out.println("[CMD]: " + msg);
//  }
  
  /**
   * Prints the usage message to System.out. This message describes all the command line
   * options.
   */  
  public static void usage() {
    System.out.println("Usage:");
    System.out.println("[" + OUTDIR_COMMAND_OPT + " output-directory]");
    System.out.println("\t\tdefault output directory to : " + TestNG.DEFAULT_OUTPUTDIR);
    System.out.println("[" + TESTCLASS_COMMAND_OPT 
        + " list of .class files or list of class names]");
    System.out.println("[" + SRC_COMMAND_OPT + " a source directory]");
    
    if (VersionInfo.IS_JDK14) {
      System.out.println("[" + ANNOTATIONS_COMMAND_OPT + " " + AnnotationTypeEnum.JAVADOC.getName() + "]");
      System.out.println("\t\tSpecifies the default annotation type to be used in suites when none is explicitly specified.");
      System.out.println("\t\tThis version of TestNG (14) only supports " + AnnotationTypeEnum.JAVADOC.getName() + " annotation type.");
      System.out.println("\t\tFor interface compatibility reasons, we allow this value to be explicitly set to " +
          AnnotationTypeEnum.JAVADOC.getName() + "\"  ");
    } 
    else {
      System.out.println("[" + ANNOTATIONS_COMMAND_OPT + " " + AnnotationTypeEnum.JAVADOC.getName() + " or " 
          + AnnotationTypeEnum.JDK.getName() + "]");
      System.out.println("\t\tSpecifies the default annotation type to be used in suites when none is explicitly");      
      System.out.println("\t\tspecified. This version of TestNG (15) supports both \"" 
          + AnnotationTypeEnum.JAVADOC.getName() + "\" and \"" + AnnotationTypeEnum.JDK.getName() + "\" annotation types.");
    }

    System.out.println("[" + GROUPS_COMMAND_OPT + " comma-separated list of group names to be run]");
    System.out.println("\t\tworks only with " + TESTCLASS_COMMAND_OPT);
    System.out.println("[" + EXCLUDED_GROUPS_COMMAND_OPT 
        + " comma-separated list of group names to be excluded]");
    System.out.println("\t\tworks only with " + TESTCLASS_COMMAND_OPT);
    System.out.println("[" + TESTRUNNER_FACTORY_COMMAND_OPT 
        + " list of .class files or list of class names implementing "
        + ITestRunnerFactory.class.getName()
        + "]");
    System.out.println("[" + LISTENER_COMMAND_OPT
        + " list of .class files or list of class names implementing "
        + ITestListener.class.getName()
        + " and/or "
        + ISuiteListener.class.getName()
        + "]");
    System.out.println("[" + PARALLEL_MODE
            + " methods|tests]");
    System.out.println("\t\trun tests in parallel using the specified mode");
    System.out.println("[" + THREAD_COUNT
            + " number of threads to use when running tests in parallel]");
    System.out.println("[" + SUITE_NAME_OPT + " name]");
    System.out.println("\t\tDefault name of test suite, if not specified in suite definition file or source code");
    System.out.println("[" + TEST_NAME_OPT + " Name]");
    System.out.println("\t\tDefault name of test, if not specified in suite definition file or source code");
    System.out.println("[" + REPORTER + " Extended configuration for custom report listener]");    
    System.out.println("[suite definition files*]");
    System.out.println("");
    System.out.println("For details, please consult the documentation at http://testng.org.");
  }
}
