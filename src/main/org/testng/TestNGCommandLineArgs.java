package org.testng;


import org.testng.internal.ClassHelper;
import org.testng.internal.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestNG/RemoteTestNG command line arguments parser.
 * 
 * @author Cedric Beust
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alexandru Popescu</a>
 */
public final class TestNGCommandLineArgs {
  public static final String SHOW_TESTNG_STACK_FRAMES = "testng.show.stack.frames";
  public static final String TEST_CLASSPATH = "testng.test.classpath";
  
  /** The test report output directory option. */
  public static final String OUTDIR_COMMAND_OPT = "-d";
  
  /** The list of test classes option. */
  public static final String TESTCLASS_COMMAND_OPT = "-testclass";
  
  /** */
  public static final String TESTJAR_COMMAND_OPT = "-testjar";
  
  /** The source directory option (for JDK1.4). */
  public static final String SRC_COMMAND_OPT = "-sourcedir";
  
  // These next two are used by the Eclipse plug-in
  public static final String PORT_COMMAND_OPT = "-port";
  public static final String HOST_COMMAND_OPT = "-host";
  
  /** The logging level option. */
  public static final String LOG = "-log";
  
  /** The default target option (for JDK 5.0+) only. */
  public static final String TARGET_COMMAND_OPT = "-target";
  
  public static final String GROUPS_COMMAND_OPT = "-groups";
  public static final String EXCLUDED_GROUPS_COMMAND_OPT = "-excludegroups";
  public static final String TESTRUNNER_FACTORY_COMMAND_OPT = "-testrunfactory";
  public static final String LISTENER_COMMAND_OPT = "-listener";
  public static final String SUITE_DEF_OPT = "testng.suite.definitions";
  public static final String JUNIT_DEF_OPT = "-junit";
  public static final String SLAVE_OPT = "-slave";
  public static final String HOSTFILE_OPT = "-hostfile";
  public static final String THREAD_COUNT = "-threadcount";

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
   * @param argv the command line options.
   * @return the parsed parameters as a map from option string to parsed values. 
   */
  public static Map parseCommandLine(final String[] argv) {
    // TODO CQ In this method, is this OK to simply ignore invalid parameters?
    
    Map<String, Object> arguments = new HashMap<String, Object>();

    for (int i = 0; i < argv.length; i++) {
      if (OUTDIR_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(OUTDIR_COMMAND_OPT, argv[i + 1].trim());
        }
        else {
          System.err.println("WARNING: missing output directory after -d.  ignored");
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
              System.err.println("WARNING: groups option is not well quoted:" + argv[i + 1]);
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
          System.err.println("WARNING: missing groups parameter after -groups. ignored");
        }
        i++;
      }
      else if (LOG.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(LOG, Integer.valueOf(argv[i + 1].trim()));
        }
        else {
          System.err.println("WARNING: missing log level after -log.  ignored");
        }
        i++;
      }
      else if (JUNIT_DEF_OPT.equalsIgnoreCase(argv[i])) {
        arguments.put(JUNIT_DEF_OPT, Boolean.TRUE);
      }
      else if (TARGET_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          if ("1.4".equals(argv[i + 1]) || "1.5".equals(argv[i + 1])) {
            arguments.put(TARGET_COMMAND_OPT, argv[i + 1]);
          }
          else {
            System.err.println(
                "WARNING: missing/invalid target argument. Must be 1.4 or 1.5. Ignoring");
          }
          i++;
        }
      }
      else if (TESTRUNNER_FACTORY_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(TESTRUNNER_FACTORY_COMMAND_OPT, fileToClass(argv[++i]));
        }
        else {
          System.err.println("WARNING: missing ITestRunnerFactory class or file argument after "
              + TESTRUNNER_FACTORY_COMMAND_OPT);
        }
      }
      else if (LISTENER_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          String[] strs = Utils.split(argv[++i], ";");
          List<Class> classes = new ArrayList<Class>();

          for (String cls : strs) {
            classes.add(fileToClass(cls));
          }

          arguments.put(LISTENER_COMMAND_OPT, classes);
        }
        else {
          System.err.println("WARNING: missing ITestListener class/file list argument after "
              + LISTENER_COMMAND_OPT);
        }
      }
      else if (TESTCLASS_COMMAND_OPT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          while ((i + 1) < argv.length) {
            String nextArg = argv[i + 1].trim();
            if (!nextArg.toLowerCase().endsWith(".xml") && !nextArg.startsWith("-")) {

              // Assume it's a class name
              List<Class> l = (List<Class>) arguments.get(TESTCLASS_COMMAND_OPT);
              if (null == l) {
                l = new ArrayList<Class>();
                arguments.put(TESTCLASS_COMMAND_OPT, l);
              }
              Class cls = fileToClass(nextArg);
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
          System.out.println("WARNING: "
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
        String clientPortNumber = null;
        if ((i + 1) < argv.length) {
          clientPortNumber = argv[i + 1].trim();
        }
        else {
          TestNG.exitWithError(SLAVE_OPT + " option should be followed by a valid port number.");
        }

        arguments.put(SLAVE_OPT, clientPortNumber);
        i++;
      }
      else if (HOSTFILE_OPT.equals(argv[i])) {
        String hostFile = null;
        if ((i + 1) < argv.length) {
          hostFile = argv[i + 1].trim();
        }
        else {
          TestNG.exitWithError(HOSTFILE_OPT + " option should be followed by the name of a file.");
        }

        arguments.put(HOSTFILE_OPT, hostFile);
        i++;
      }
      else if (THREAD_COUNT.equalsIgnoreCase(argv[i])) {
        if ((i + 1) < argv.length) {
          arguments.put(THREAD_COUNT, argv[i + 1]);
          i++;
        }
      }
      
      //
      // Unknown option
      //
      else if (argv[i].startsWith("-")) {
        TestNG.exitWithError("Unknown option: " + argv[i]);
      }
      else {
        List<String> suiteDefs = new ArrayList<String>();

        for (int k = i; k < argv.length; k++) {
          String file = argv[k].trim();
          if (file.toLowerCase().endsWith(".xml")) {
            suiteDefs.add(file);
            i++;
          }
        }

        arguments.put(SUITE_DEF_OPT, suiteDefs);
      }
    }

    return arguments;
  }


  /**
   * Returns the Class object corresponding to the given name. The name may
   * be of the following form:
   * <ul>
   * <li>A class name: "org.testng.TestNG"</li>  
   * <li>A class file name: "/testng/src/org/testng/TestNG.class"</li>  
   * <li>A class source name: "d:\testng\src\org\testng\TestNG.java"</li>  
   * </ul>  
   *
   * @param file the class name. 
   * @return the class corresponding to the name specified.
   */
  private static Class fileToClass(String file) {
    Class result = null;
    int classIndex = file.indexOf(".class");
    if (-1 == classIndex) {
      classIndex = file.indexOf(".java");
      if (-1 == classIndex) {
        // Doesn't end in .java or .class, assume it's a class name
        result = ClassHelper.forName(file);

        if (null == result) {
          throw new TestNGException("Cannot load class from file: " + file);
        }

        return result;
      }
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
    System.out.println("\t\tdefault output directory to : " + "test-output");
    System.out.println("[" + TESTCLASS_COMMAND_OPT 
        + " list of .class files or list of class names]");
    System.out.println("[" + SRC_COMMAND_OPT + " a source directory]");
    System.out.println("[" + TARGET_COMMAND_OPT + " 1.4 or 1.5]"); 
    System.out.println("\t\tused only with JDK1.5 to specify the " 
        + "annotation type used in test classes; default target: 1.5");
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
    System.out.println("[" + THREAD_COUNT
        + " number of threads to use"
        + "]");
    System.out.println("[suite definition files*]");
    System.out.println("");
    System.out.println("For details please consult documentation.");
  }
  
}
