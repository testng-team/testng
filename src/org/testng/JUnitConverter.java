package org.testng;

import org.testng.collections.Maps;
import org.testng.junit.JUnitDirectoryConverter;

import java.io.File;
import java.util.Map;

/**
 * Convert JUnit files into TestNG by annotating them.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href="mailto:the_mindstorm@evolva.ro">the_mindstorm</a>
 */
public class JUnitConverter {
   private static final String USE_ANNOTATION_OPT = "-annotation";
   private static final String USE_JAVADOC_OPT  = "-javadoc";
   private static final String SRC_DIR_OPT = "-srcdir";
   private static final String SOURCE_OPT = "-source";
   private static final String OUT_DIR_OPT = "-d";
   private static final String OVERWRITE_OPT = "-overwrite";
   private static final String QUIET = "-quiet";
   // Deprecated, maintained for backward compatibility
   private static final String RESTORE_OPT  = "-restore";
   private static final String GROUPS_OPT  = "-groups";
   
   private static int m_logLevel = 1;

   public static void main(String[] args) {
      if(args.length == 0) {
         usage();

         return;
      }

      Map params = extractOptions(args);
      
      if (null != params.get(QUIET)) {
        m_logLevel = 0;
      }

      if(null != params.get(USE_ANNOTATION_OPT) && null != params.get(USE_JAVADOC_OPT)) {
         TestNG.exitWithError("Cannot use both --annotation and --javadoc options");
      }

      if(null == params.get(USE_ANNOTATION_OPT) && null == params.get(USE_JAVADOC_OPT)) {
         TestNG.exitWithError("One of --annotation and --javadoc option is required");
      }

      boolean useAnnotations = null != params.get(USE_ANNOTATION_OPT);

      if(null == params.get(SRC_DIR_OPT)) {
         TestNG.exitWithError("The source directory cannot be null");
      }

      String srcPath = (String) params.get(SRC_DIR_OPT);
      File src = new File(srcPath);

      if(!src.exists() || !src.isDirectory()) {
         TestNG.exitWithError("Invalid source directory: " + src.getAbsolutePath());
      }

      boolean overwrite = null != params.get(OVERWRITE_OPT);

      if(null != params.get(OUT_DIR_OPT) && overwrite) {
         TestNG.exitWithError("Cannot use both -d and --overwrite options");
      }

      if(null == params.get(OUT_DIR_OPT) && !overwrite) {
         TestNG.exitWithError("One of -d and --overwrite options is required");
      }

      String outPath = overwrite ? srcPath : (String) params.get(OUT_DIR_OPT);
      
      String groupsOpt = (String) params.get(GROUPS_OPT);
      String[] groups = null;
      if (groupsOpt != null) {
        groups = groupsOpt.split("[ ,]");
      }

      JUnitDirectoryConverter convertor = new JUnitDirectoryConverter(new File(srcPath),
            new File(outPath),
            (String) params.get(SOURCE_OPT),
            useAnnotations, groups);

      int result = convertor.convert();

      switch(result) {
         case -1:
            log("Generation failed. Consult messages.");
            break;
         case 0:
            log("No tests were generated");
         default:
            log(result + " tests were generated");
      }
   }

   /**
    * Extract command line options.
    */
   private static Map extractOptions(String[] args) {
      Map options = Maps.newHashMap();

      for (int i = 0; i < args.length; i++) {
      if (USE_ANNOTATION_OPT.equals(args[i])) {
        options.put(USE_ANNOTATION_OPT, Boolean.TRUE);
      }
      else if (USE_JAVADOC_OPT.equals(args[i])) {
        options.put(USE_JAVADOC_OPT, Boolean.TRUE);
      }
      else if (SRC_DIR_OPT.equals(args[i])) {
        if (i + 1 < args.length) {
          options.put(SRC_DIR_OPT, args[i + 1]);
          i++;
        }
      }
      else if (OUT_DIR_OPT.equals(args[i])) {
        if (i + 1 < args.length) {
          options.put(OUT_DIR_OPT, args[i + 1]);
          i++;
        }
      }
      else if (OVERWRITE_OPT.equals(args[i])) {
        options.put(OVERWRITE_OPT, Boolean.TRUE);
      }
      else if (RESTORE_OPT.equals(args[i])) {
        // ignore
      }
      else if (QUIET.equals(args[i])) {
        options.put(QUIET, Boolean.TRUE);
      }
      else if (SOURCE_OPT.equals(args[i])) {
        if (i + 1 < args.length) {
          options.put(SOURCE_OPT, args[i + 1]);
          i++;
        }
      }
      else if (GROUPS_OPT.equals(args[i])) {
        if (i + 1 < args.length) {
          options.put(GROUPS_OPT , args[i + 1]);
          i++;
        }
      }
    }

      return options;
   }

   /**
     * Prints usage info to console.
     */
   private static void usage() {
      System.out.println("Converts JUnit test cases to TestNG.");
      System.out.println("Usage: java -cp <> org.testng.JUnitConverter"
            + " (-annotation | -javadoc) -srcdir <source_dir> "
            + " (-d <output_dir> -overwrite)"
            + " -groups <groups>"
            + " [-source <release>]");
      System.out.println("");
      System.out.println("-annotation\t Generated tests will use JDK1.5 annotations");
      System.out.println("-javadoc\t Generated tests will use javadoc like annotations");
      System.out.println("-srcdir\t Source directory containing JUnit tests");
      System.out.println("-d\t\t Output directory for resulting TestNG tests and configuration xml");
      System.out.println("-overwrite\t Overwrite the original JUnit files with the new ones"
            + "\n\t\t The flag cannot be used when -d is used.");
      System.out.println("-source\t Provide source compatibility with specified release");
      System.out.println("-groups\t The groups that the methods will belong to");
      System.out.println("-quiet\t Don't display any output");
   }

   private static boolean m_verbose = false;
   
   public static void log(String s) {
     if (m_verbose) {
      System.out.println("[JUnitConverter] " + s);
     }
  }
   
   public static int getLogLevel() {
     return m_logLevel;
   }
   
   public static void ppp(String s) {
    System.out.println("[JUnitConverter] " + s);
  }
}
